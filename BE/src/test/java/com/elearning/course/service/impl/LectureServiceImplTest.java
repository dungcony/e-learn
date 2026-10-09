package com.elearning.course.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.course.dto.request.ExerciseAnswerRequest;
import com.elearning.course.dto.request.ExerciseCreateRequest;
import com.elearning.course.dto.request.ExerciseQuestionRequest;
import com.elearning.course.dto.request.ExerciseUpdateRequest;
import com.elearning.course.dto.request.LectureCreateRequest;
import com.elearning.course.dto.response.CourseLectureIds;
import com.elearning.course.dto.response.ExerciseAnswerKey;
import com.elearning.course.dto.response.ExerciseDetailResponse;
import com.elearning.course.dto.response.LearningLectureResponse;
import com.elearning.course.dto.response.LectureDetailResponse;
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
import com.elearning.course.repository.QuestionRepository;
import com.elearning.course.validator.ExerciseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LectureServiceImplTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AnswerRepository answerRepository;

    private LectureServiceImpl service;

    private final UUID teacherId = UUID.randomUUID();
    private final UUID courseId = UUID.randomUUID();
    private final UUID lectureId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new LectureServiceImpl(courseRepository, lectureRepository, exerciseRepository, questionRepository,
                answerRepository, Mappers.getMapper(LectureMapper.class), Mappers.getMapper(ExerciseMapper.class),
                new ExerciseValidator());
    }

    // ---- bài giảng ----

    @Test
    void createLecture_inAnotherTeachersCourseIsNotFound() {
        when(courseRepository.findByIdAndTeacherId(courseId, teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLecture(teacherId, courseId,
                new LectureCreateRequest("Bài 1", null, "https://x.y/a.mp4")))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        verify(lectureRepository, never()).save(any());
    }

    @Test
    void createLecture_setsCourseAndCreatorFromUrlAndToken() {
        when(courseRepository.findByIdAndTeacherId(courseId, teacherId))
                .thenReturn(Optional.of(com.elearning.course.entity.Course.builder().id(courseId).build()));
        when(lectureRepository.save(any(Lecture.class))).thenAnswer(inv -> inv.getArgument(0));

        LectureDetailResponse response = service.createLecture(teacherId, courseId,
                new LectureCreateRequest("Bài 1", "Mô tả", "https://x.y/a.mp4"));

        assertThat(response.courseId()).isEqualTo(courseId);
        assertThat(response.createdBy()).isEqualTo(teacherId);
        assertThat(response.exercises()).isEmpty();
    }

    @Test
    void deleteLecture_softDeletes() {
        Lecture lecture = lecture();
        when(courseRepository.findByIdAndTeacherId(courseId, teacherId))
                .thenReturn(Optional.of(com.elearning.course.entity.Course.builder().id(courseId).build()));
        when(lectureRepository.findByIdAndCourseId(lectureId, courseId)).thenReturn(Optional.of(lecture));

        service.deleteLecture(teacherId, courseId, lectureId);

        assertThat(lecture.getDeletedAt()).isNotNull();
    }

    // ---- bài tập ----

    @Test
    void createExercise_inAnotherTeachersLectureIsNotFound() {
        when(lectureRepository.findByIdAndTeacherId(lectureId, teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createExercise(teacherId, lectureId, createRequest(validQuestion())))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void createExercise_twoCorrectAnswersFailsBeforeSavingAnything() {
        when(lectureRepository.findByIdAndTeacherId(lectureId, teacherId)).thenReturn(Optional.of(lecture()));

        assertThatThrownBy(() -> service.createExercise(teacherId, lectureId,
                createRequest(question(true, true, false, false))))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("EXERCISE_ANSWER_INVALID"));
        verify(exerciseRepository, never()).save(any());
        verify(questionRepository, never()).saveAll(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createExercise_savesQuestionsBeforeAnswersWithPositionsAndCorrectFlag() {
        when(lectureRepository.findByIdAndTeacherId(lectureId, teacherId)).thenReturn(Optional.of(lecture()));
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(inv -> inv.getArgument(0));

        ExerciseDetailResponse response = service.createExercise(teacherId, lectureId,
                createRequest(question(false, false, true, false), question(true, false, false, false)));

        // Đáp án có khóa ngoại tới câu hỏi nên phải lưu sau.
        InOrder order = inOrder(questionRepository, answerRepository);
        ArgumentCaptor<List<Question>> questions = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Answer>> answers = ArgumentCaptor.forClass(List.class);
        order.verify(questionRepository).saveAll(questions.capture());
        order.verify(answerRepository).saveAll(answers.capture());
        assertThat(questions.getValue()).extracting(Question::getPosition).containsExactly(0, 1);
        assertThat(answers.getValue()).hasSize(8);
        assertThat(answers.getValue()).filteredOn(Answer::isCorrect).hasSize(2);
        assertThat(response.questions()).hasSize(2);
        assertThat(response.questions().get(0).answers().get(2).isCorrect()).isTrue();
        assertThat(response.questions().get(1).answers().get(0).isCorrect()).isTrue();
    }

    @Test
    void updateExercise_softDeletesOldQuestionsAndAnswersThenCreatesNewOnes() {
        Exercise exercise = exercise();
        Question oldQuestion = Question.builder().id(UUID.randomUUID()).exerciseId(exercise.getId()).content("Cũ").build();
        Answer oldAnswer = Answer.builder().id(UUID.randomUUID()).questionId(oldQuestion.getId()).content("a").build();
        when(lectureRepository.findByIdAndTeacherId(lectureId, teacherId)).thenReturn(Optional.of(lecture()));
        when(exerciseRepository.findByIdAndLectureId(exercise.getId(), lectureId)).thenReturn(Optional.of(exercise));
        when(questionRepository.findByExerciseIdOrderByPositionAsc(exercise.getId())).thenReturn(List.of(oldQuestion));
        when(answerRepository.findByQuestionIdInOrderByPositionAsc(anyCollection())).thenReturn(List.of(oldAnswer));

        ExerciseDetailResponse response = service.updateExercise(teacherId, lectureId, exercise.getId(),
                new ExerciseUpdateRequest("Tên mới", "Mô tả mới", List.of(validQuestion())));

        assertThat(oldQuestion.getDeletedAt()).isNotNull();
        assertThat(oldAnswer.getDeletedAt()).isNotNull();
        assertThat(exercise.getTitle()).isEqualTo("Tên mới");
        assertThat(response.questions()).hasSize(1);
        assertThat(response.questions().get(0).id()).isNotEqualTo(oldQuestion.getId());
    }

    @Test
    void deleteExercise_softDeletes() {
        Exercise exercise = exercise();
        when(lectureRepository.findByIdAndTeacherId(lectureId, teacherId)).thenReturn(Optional.of(lecture()));
        when(exerciseRepository.findByIdAndLectureId(exercise.getId(), lectureId)).thenReturn(Optional.of(exercise));

        service.deleteExercise(teacherId, lectureId, exercise.getId());

        assertThat(exercise.getDeletedAt()).isNotNull();
    }

    // ---- API cho learning ----

    @Test
    void getExerciseAnswerKey_exposesCorrectAnswerAndAnswerIdsPerQuestion() {
        Exercise exercise = exercise();
        Question q = Question.builder().id(UUID.randomUUID()).exerciseId(exercise.getId()).content("?").position(0).build();
        Answer wrong = Answer.builder().id(UUID.randomUUID()).questionId(q.getId()).position(0).build();
        Answer right = Answer.builder().id(UUID.randomUUID()).questionId(q.getId()).position(1).correct(true).build();
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture()));
        when(questionRepository.findByExerciseIdOrderByPositionAsc(exercise.getId())).thenReturn(List.of(q));
        when(answerRepository.findByQuestionIdInOrderByPositionAsc(anyCollection())).thenReturn(List.of(wrong, right));

        ExerciseAnswerKey key = service.getExerciseAnswerKey(exercise.getId());

        assertThat(key.courseId()).isEqualTo(courseId);
        assertThat(key.lectureId()).isEqualTo(lectureId);
        assertThat(key.questions()).hasSize(1);
        assertThat(key.questions().get(0).correctAnswerId()).isEqualTo(right.getId());
        assertThat(key.questions().get(0).answerIds()).containsExactly(wrong.getId(), right.getId());
    }

    @Test
    void getExerciseAnswerKey_exerciseOfDeletedLectureIsNotFound() {
        Exercise exercise = exercise();
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExerciseAnswerKey(exercise.getId()))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
    }

    @Test
    void getLearningLecture_hasExercisesWithQuestionsButNoCorrectFlag() {
        Exercise exercise = exercise();
        Question q = Question.builder().id(UUID.randomUUID()).exerciseId(exercise.getId()).content("?").position(0).build();
        Answer a = Answer.builder().id(UUID.randomUUID()).questionId(q.getId()).content("A").correct(true).build();
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture()));
        when(exerciseRepository.findByLectureIdOrderByCreatedAtAscIdAsc(lectureId)).thenReturn(List.of(exercise));
        when(questionRepository.findByExerciseIdInOrderByPositionAsc(anyCollection())).thenReturn(List.of(q));
        when(answerRepository.findByQuestionIdInOrderByPositionAsc(anyCollection())).thenReturn(List.of(a));

        LearningLectureResponse response = service.getLearningLecture(lectureId);

        assertThat(response.exercises()).hasSize(1);
        assertThat(response.exercises().get(0).questions().get(0).answers())
                .extracting(x -> x.content()).containsExactly("A");
        assertThat(response.exercises().get(0).questions().get(0).answers().get(0).getClass().getRecordComponents())
                .extracting(c -> c.getName()).doesNotContain("isCorrect", "correct");
    }

    @Test
    void getLectureIds_groupsByCourseAndKeepsCoursesWithoutLectures() {
        UUID emptyCourse = UUID.randomUUID();
        UUID lec1 = UUID.randomUUID();
        UUID lec2 = UUID.randomUUID();
        when(lectureRepository.findLectureIdsByCourseIds(anyCollection())).thenReturn(List.of(
                view(courseId, lec1), view(courseId, lec2)));

        List<CourseLectureIds> result = service.getLectureIds(List.of(courseId, emptyCourse));

        assertThat(result).containsExactly(
                new CourseLectureIds(courseId, List.of(lec1, lec2)),
                new CourseLectureIds(emptyCourse, List.of()));
    }

    @Test
    void getLectureIds_emptyInputSkipsQuery() {
        assertThat(service.getLectureIds(List.of())).isEmpty();
        verify(lectureRepository, never()).findLectureIdsByCourseIds(anyCollection());
    }

    private LectureIdView view(UUID course, UUID id) {
        return new LectureIdView() {
            public UUID getCourseId() { return course; }
            public UUID getId() { return id; }
        };
    }

    private Lecture lecture() {
        return Lecture.builder().id(lectureId).courseId(courseId).title("Bài 1").contentUrl("https://x.y")
                .createdBy(teacherId).build();
    }

    private Exercise exercise() {
        return Exercise.builder().id(UUID.randomUUID()).lectureId(lectureId).title("BT").description("d")
                .createdBy(teacherId).build();
    }

    private ExerciseCreateRequest createRequest(ExerciseQuestionRequest... questions) {
        return new ExerciseCreateRequest("Bài tập 1", "Mô tả", List.of(questions));
    }

    private ExerciseQuestionRequest validQuestion() {
        return question(true, false, false, false);
    }

    private ExerciseQuestionRequest question(boolean... correct) {
        return new ExerciseQuestionRequest("Câu hỏi?", java.util.stream.IntStream.range(0, correct.length)
                .mapToObj(i -> new ExerciseAnswerRequest("Đáp án " + i, correct[i])).toList());
    }
}
