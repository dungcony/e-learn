package com.elearning.course.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.response.PageRequestParams;
import com.elearning.course.dto.request.ExerciseAnswerRequest;
import com.elearning.course.dto.request.ExerciseCreateRequest;
import com.elearning.course.dto.request.ExerciseQuestionRequest;
import com.elearning.course.dto.request.ExerciseUpdateRequest;
import com.elearning.course.dto.request.LectureCreateRequest;
import com.elearning.course.dto.request.LectureUpdateRequest;
import com.elearning.course.dto.response.CourseLectureIds;
import com.elearning.course.dto.response.ExerciseAnswerKey;
import com.elearning.course.dto.response.ExerciseDetailResponse;
import com.elearning.course.dto.response.ExerciseSummaryResponse;
import com.elearning.course.dto.response.LearningExerciseResponse;
import com.elearning.course.dto.response.LearningLectureResponse;
import com.elearning.course.dto.response.LectureDetailResponse;
import com.elearning.course.dto.response.LectureSummaryResponse;
import com.elearning.course.dto.response.QuestionAnswerKey;
import com.elearning.course.entity.Answer;
import com.elearning.course.entity.Exercise;
import com.elearning.course.entity.Lecture;
import com.elearning.course.entity.Question;
import com.elearning.course.mapper.ExerciseMapper;
import com.elearning.course.mapper.LectureMapper;
import com.elearning.course.repository.AnswerRepository;
import com.elearning.course.repository.CourseRepository;
import com.elearning.course.repository.ExerciseRepository;
import com.elearning.course.repository.LectureIdView;
import com.elearning.course.repository.LectureRepository;
import com.elearning.course.repository.LectureSpecification;
import com.elearning.course.repository.QuestionRepository;
import com.elearning.course.service.LectureService;
import com.elearning.course.validator.ExerciseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private static final Map<String, String> SORTABLE_FIELDS = Map.of("created_at", "createdAt", "title", "title");

    private final CourseRepository courseRepository;
    private final LectureRepository lectureRepository;
    private final ExerciseRepository exerciseRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final LectureMapper lectureMapper;
    private final ExerciseMapper exerciseMapper;
    private final ExerciseValidator exerciseValidator;

    // Câu hỏi và đáp án vừa lưu của một bài tập.
    private record QuestionSet(List<Question> questions, Map<UUID, List<Answer>> answersByQuestion) {
    }

    // ---- bài giảng của giảng viên ----

    @Override
    @Transactional(readOnly = true)
    public Page<LectureSummaryResponse> searchLectures(UUID teacherId, UUID courseId, String name,
                                                       PageRequestParams params) {
        requireOwnedCourse(teacherId, courseId);
        return lectureRepository
                .findAll(LectureSpecification.search(courseId, name), params.toPageable(SORTABLE_FIELDS, "createdAt"))
                .map(lectureMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureDetailResponse getLecture(UUID teacherId, UUID courseId, UUID id) {
        return toLectureDetail(findOwnedLecture(teacherId, courseId, id));
    }

    @Override
    @Transactional
    public LectureDetailResponse createLecture(UUID teacherId, UUID courseId, LectureCreateRequest request) {
        requireOwnedCourse(teacherId, courseId);
        Lecture lecture = lectureMapper.toNewLecture(request);
        lecture.setId(UUID.randomUUID());
        lecture.setCourseId(courseId);
        lecture.setCreatedBy(teacherId);
        return lectureMapper.toDetailResponse(lectureRepository.save(lecture), List.of());
    }

    @Override
    @Transactional
    public LectureDetailResponse updateLecture(UUID teacherId, UUID courseId, UUID id, LectureUpdateRequest request) {
        Lecture lecture = findOwnedLecture(teacherId, courseId, id);
        lectureMapper.updateLecture(request, lecture);
        return toLectureDetail(lecture);
    }

    @Override
    @Transactional
    public void deleteLecture(UUID teacherId, UUID courseId, UUID id) {
        findOwnedLecture(teacherId, courseId, id).setDeletedAt(Instant.now());
    }

    // ---- bài tập của giảng viên ----

    @Override
    @Transactional
    public ExerciseDetailResponse createExercise(UUID teacherId, UUID lectureId, ExerciseCreateRequest request) {
        requireOwnedLecture(teacherId, lectureId);
        exerciseValidator.validateSingleCorrectAnswer(request.questions());
        Exercise exercise = exerciseRepository.save(Exercise.builder()
                .id(UUID.randomUUID())
                .lectureId(lectureId)
                .title(request.title())
                .description(request.description())
                .createdBy(teacherId)
                .build());
        QuestionSet saved = saveQuestions(exercise.getId(), request.questions());
        return exerciseMapper.toDetailResponse(exercise, saved.questions(), saved.answersByQuestion());
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseDetailResponse getExercise(UUID teacherId, UUID lectureId, UUID id) {
        requireOwnedLecture(teacherId, lectureId);
        Exercise exercise = findExercise(id, lectureId);
        List<Question> questions = questionRepository.findByExerciseIdOrderByPositionAsc(id);
        return exerciseMapper.toDetailResponse(exercise, questions, loadAnswers(questions));
    }

    @Override
    @Transactional
    public ExerciseDetailResponse updateExercise(UUID teacherId, UUID lectureId, UUID id, ExerciseUpdateRequest request) {
        requireOwnedLecture(teacherId, lectureId);
        Exercise exercise = findExercise(id, lectureId);
        exerciseValidator.validateSingleCorrectAnswer(request.questions());
        exercise.setTitle(request.title());
        exercise.setDescription(request.description());

        // Xóa mềm câu hỏi, đáp án cũ rồi tạo mới để bài làm cũ của học viên vẫn trỏ được tới câu cũ.
        Instant now = Instant.now();
        List<Question> oldQuestions = questionRepository.findByExerciseIdOrderByPositionAsc(id);
        loadAnswers(oldQuestions).values().forEach(answers -> answers.forEach(a -> a.setDeletedAt(now)));
        oldQuestions.forEach(q -> q.setDeletedAt(now));

        QuestionSet saved = saveQuestions(id, request.questions());
        return exerciseMapper.toDetailResponse(exercise, saved.questions(), saved.answersByQuestion());
    }

    @Override
    @Transactional
    public void deleteExercise(UUID teacherId, UUID lectureId, UUID id) {
        requireOwnedLecture(teacherId, lectureId);
        findExercise(id, lectureId).setDeletedAt(Instant.now());
    }

    // ---- API công khai cho module learning ----

    @Override
    @Transactional(readOnly = true)
    public List<LectureSummaryResponse> getLectureSummaries(UUID courseId) {
        return lectureRepository.findByCourseIdOrderByCreatedAtAscIdAsc(courseId).stream()
                .map(lectureMapper::toSummaryResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LearningLectureResponse getLearningLecture(UUID lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(this::lectureNotFound);
        List<Exercise> exercises = exerciseRepository.findByLectureIdOrderByCreatedAtAscIdAsc(lectureId);
        Map<UUID, List<Question>> questionsByExercise = questionRepository
                .findByExerciseIdInOrderByPositionAsc(ids(exercises, Exercise::getId)).stream()
                .collect(Collectors.groupingBy(Question::getExerciseId, LinkedHashMap::new, Collectors.toList()));
        Map<UUID, List<Answer>> answersByQuestion = loadAnswers(
                questionsByExercise.values().stream().flatMap(List::stream).toList());

        List<LearningExerciseResponse> exerciseResponses = exercises.stream()
                .map(e -> exerciseMapper.toLearningResponse(
                        e, questionsByExercise.getOrDefault(e.getId(), List.of()), answersByQuestion))
                .toList();
        return new LearningLectureResponse(lecture.getId(), lecture.getCourseId(), lecture.getTitle(),
                lecture.getDescription(), lecture.getContentUrl(), exerciseResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseAnswerKey getExerciseAnswerKey(UUID exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài tập."));
        Lecture lecture = lectureRepository.findById(exercise.getLectureId()).orElseThrow(this::lectureNotFound);
        List<Question> questions = questionRepository.findByExerciseIdOrderByPositionAsc(exerciseId);
        Map<UUID, List<Answer>> answersByQuestion = loadAnswers(questions);

        List<QuestionAnswerKey> keys = questions.stream().map(q -> {
            List<Answer> answers = answersByQuestion.getOrDefault(q.getId(), List.of());
            UUID correctId = answers.stream().filter(Answer::isCorrect).map(Answer::getId).findFirst().orElse(null);
            return new QuestionAnswerKey(q.getId(), answers.stream().map(Answer::getId).toList(), correctId);
        }).toList();
        return new ExerciseAnswerKey(exerciseId, lecture.getId(), lecture.getCourseId(), keys);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseLectureIds> getLectureIds(Collection<UUID> courseIds) {
        if (courseIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<UUID>> byCourse = new LinkedHashMap<>();
        courseIds.forEach(id -> byCourse.put(id, new ArrayList<>()));
        for (LectureIdView view : lectureRepository.findLectureIdsByCourseIds(courseIds)) {
            byCourse.get(view.getCourseId()).add(view.getId());
        }
        return byCourse.entrySet().stream().map(e -> new CourseLectureIds(e.getKey(), e.getValue())).toList();
    }

    // ---- nội bộ ----

    private LectureDetailResponse toLectureDetail(Lecture lecture) {
        List<ExerciseSummaryResponse> exercises = exerciseRepository
                .findByLectureIdOrderByCreatedAtAscIdAsc(lecture.getId()).stream()
                .map(exerciseMapper::toSummaryResponse).toList();
        return lectureMapper.toDetailResponse(lecture, exercises);
    }

    private QuestionSet saveQuestions(UUID exerciseId, List<ExerciseQuestionRequest> requests) {
        List<Question> questions = new ArrayList<>();
        List<Answer> answers = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            ExerciseQuestionRequest request = requests.get(i);
            Question question = Question.builder().id(UUID.randomUUID()).exerciseId(exerciseId)
                    .content(request.content()).position(i).build();
            questions.add(question);
            for (int j = 0; j < request.answers().size(); j++) {
                ExerciseAnswerRequest answer = request.answers().get(j);
                answers.add(Answer.builder().id(UUID.randomUUID()).questionId(question.getId())
                        .content(answer.content()).correct(answer.isCorrect()).position(j).build());
            }
        }
        // Lưu câu hỏi trước đáp án vì đáp án có khóa ngoại tới câu hỏi.
        questionRepository.saveAll(questions);
        answerRepository.saveAll(answers);
        return new QuestionSet(questions, answers.stream().collect(Collectors.groupingBy(Answer::getQuestionId)));
    }

    private Map<UUID, List<Answer>> loadAnswers(List<Question> questions) {
        if (questions.isEmpty()) {
            return Map.of();
        }
        return answerRepository.findByQuestionIdInOrderByPositionAsc(ids(questions, Question::getId)).stream()
                .collect(Collectors.groupingBy(Answer::getQuestionId));
    }

    private <T> List<UUID> ids(Collection<T> items, java.util.function.Function<T, UUID> idOf) {
        return items.stream().map(idOf).toList();
    }

    private void requireOwnedCourse(UUID teacherId, UUID courseId) {
        courseRepository.findByIdAndTeacherId(courseId, teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
    }

    private Lecture findOwnedLecture(UUID teacherId, UUID courseId, UUID id) {
        requireOwnedCourse(teacherId, courseId);
        return lectureRepository.findByIdAndCourseId(id, courseId).orElseThrow(this::lectureNotFound);
    }

    private Lecture requireOwnedLecture(UUID teacherId, UUID lectureId) {
        return lectureRepository.findByIdAndTeacherId(lectureId, teacherId).orElseThrow(this::lectureNotFound);
    }

    private Exercise findExercise(UUID id, UUID lectureId) {
        return exerciseRepository.findByIdAndLectureId(id, lectureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài tập."));
    }

    private BusinessException lectureNotFound() {
        return new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài giảng.");
    }
}
