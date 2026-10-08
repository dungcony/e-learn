package com.elearning.learning.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.course.dto.response.ExerciseAnswerKey;
import com.elearning.course.dto.response.LearningLectureResponse;
import com.elearning.course.dto.response.LectureSummaryResponse;
import com.elearning.course.dto.response.QuestionAnswerKey;
import com.elearning.course.service.LectureService;
import com.elearning.learning.dto.request.SubmissionAnswerRequest;
import com.elearning.learning.dto.request.SubmissionSaveRequest;
import com.elearning.learning.dto.response.MyExerciseResponse;
import com.elearning.learning.dto.response.MyLectureDetailResponse;
import com.elearning.learning.dto.response.MyLectureSummaryResponse;
import com.elearning.learning.dto.response.SubmissionAnswerResponse;
import com.elearning.learning.dto.response.SubmissionResponse;
import com.elearning.learning.entity.Submission;
import com.elearning.learning.entity.SubmissionAnswer;
import com.elearning.learning.enums.SubmissionStatus;
import com.elearning.learning.repository.LectureCompletionRepository;
import com.elearning.learning.repository.SubmissionAnswerRepository;
import com.elearning.learning.repository.SubmissionRepository;
import com.elearning.learning.service.LearningService;
import com.elearning.learning.validator.LearningAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final LearningAccessValidator accessValidator;
    private final LectureService lectureService;
    private final LectureCompletionRepository completionRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MyLectureSummaryResponse> getMyLectures(UUID studentId, UUID courseId) {
        accessValidator.requireLearningAccess(studentId, courseId);
        List<LectureSummaryResponse> lectures = lectureService.getLectureSummaries(courseId);
        Set<UUID> completed = new HashSet<>(completionRepository.findCompletedLectureIds(studentId, List.of(courseId)));
        return lectures.stream().map(l -> new MyLectureSummaryResponse(l.id(), l.title(), completed.contains(l.id()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MyLectureDetailResponse getMyLecture(UUID studentId, UUID courseId, UUID lectureId) {
        accessValidator.requireLearningAccess(studentId, courseId);
        LearningLectureResponse lecture = lectureService.getLearningLecture(lectureId);
        if (!lecture.courseId().equals(courseId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài giảng.");
        }
        boolean completed = completionRepository.existsByStudentIdAndLectureId(studentId, lectureId);
        Map<UUID, SubmissionStatus> statusByExercise = submissionRepository
                .findByStudentIdAndExerciseIdIn(studentId, lecture.exercises().stream().map(e -> e.id()).toList()).stream()
                .collect(Collectors.toMap(Submission::getExerciseId, Submission::getStatus));
        List<MyExerciseResponse> exercises = lecture.exercises().stream()
                .map(e -> new MyExerciseResponse(e.id(), e.title(), e.description(), statusByExercise.get(e.id()), e.questions()))
                .toList();
        return new MyLectureDetailResponse(lecture.id(), lecture.courseId(), lecture.title(), lecture.description(),
                lecture.contentUrl(), completed, exercises);
    }

    @Override
    @Transactional
    public void completeLecture(UUID studentId, UUID lectureId) {
        UUID courseId = lectureService.getCourseIdOfLecture(lectureId);
        accessValidator.requireLearningAccess(studentId, courseId);
        completionRepository.insertIfAbsent(UUID.randomUUID(), studentId, courseId, lectureId, Instant.now());
    }

    @Override
    @Transactional
    public SubmissionResponse saveSubmission(UUID studentId, UUID exerciseId, SubmissionSaveRequest request) {
        ExerciseAnswerKey key = lectureService.getExerciseAnswerKey(exerciseId);
        accessValidator.requireLearningAccess(studentId, key.courseId());
        Map<UUID, UUID> chosen = validateChosenAnswers(key, request);

        Submission submission = submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId)
                .orElseGet(() -> createDraft(studentId, exerciseId));
        if (submission.getStatus() == SubmissionStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.SUBMISSION_ALREADY_SUBMITTED);
        }

        Map<UUID, SubmissionAnswer> existing = submissionAnswerRepository.findBySubmissionId(submission.getId()).stream()
                .collect(Collectors.toMap(SubmissionAnswer::getQuestionId, Function.identity()));
        List<SubmissionAnswer> toInsert = new java.util.ArrayList<>();
        chosen.forEach((questionId, answerId) -> {
            SubmissionAnswer current = existing.get(questionId);
            if (current != null) {
                current.setAnswerId(answerId);
            } else {
                SubmissionAnswer created = SubmissionAnswer.builder().id(UUID.randomUUID())
                        .submissionId(submission.getId()).questionId(questionId).answerId(answerId).build();
                toInsert.add(created);
                existing.put(questionId, created);
            }
        });
        submissionAnswerRepository.saveAll(toInsert);
        return toResponse(submission, existing.values(), key);
    }

    @Override
    @Transactional
    public SubmissionResponse submit(UUID studentId, UUID exerciseId) {
        ExerciseAnswerKey key = lectureService.getExerciseAnswerKey(exerciseId);
        accessValidator.requireLearningAccess(studentId, key.courseId());
        Submission submission = submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBMISSION_INCOMPLETE));
        if (submission.getStatus() == SubmissionStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.SUBMISSION_ALREADY_SUBMITTED);
        }

        List<SubmissionAnswer> answers = submissionAnswerRepository.findBySubmissionId(submission.getId());
        Map<UUID, SubmissionAnswer> byQuestion = answers.stream()
                .collect(Collectors.toMap(SubmissionAnswer::getQuestionId, Function.identity()));
        int score = 0;
        for (QuestionAnswerKey question : key.questions()) {
            SubmissionAnswer answer = byQuestion.get(question.questionId());
            if (answer == null) {
                throw new BusinessException(ErrorCode.SUBMISSION_INCOMPLETE);
            }
            boolean correct = answer.getAnswerId().equals(question.correctAnswerId());
            answer.setCorrect(correct);
            if (correct) {
                score++;
            }
        }
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setScore(score);
        submission.setTotalQuestions(key.questions().size());
        submission.setSubmittedAt(Instant.now());
        return toResponse(submission, answers, key);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(UUID studentId, UUID exerciseId) {
        ExerciseAnswerKey key = lectureService.getExerciseAnswerKey(exerciseId);
        accessValidator.requireLearningAccess(studentId, key.courseId());
        Submission submission = submissionRepository.findByStudentIdAndExerciseId(studentId, exerciseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Bạn chưa làm bài tập này."));
        return toResponse(submission, submissionAnswerRepository.findBySubmissionId(submission.getId()), key);
    }

    // Mỗi câu hỏi tối đa một đáp án, câu hỏi phải thuộc bài tập và đáp án phải thuộc câu hỏi đó.
    private Map<UUID, UUID> validateChosenAnswers(ExerciseAnswerKey key, SubmissionSaveRequest request) {
        Map<UUID, QuestionAnswerKey> questions = key.questions().stream()
                .collect(Collectors.toMap(QuestionAnswerKey::questionId, Function.identity()));
        Map<UUID, UUID> chosen = new LinkedHashMap<>();
        for (SubmissionAnswerRequest answer : request.answers()) {
            QuestionAnswerKey question = questions.get(answer.questionId());
            if (question == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Câu hỏi không thuộc bài tập này.");
            }
            if (!question.answerIds().contains(answer.answerId())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Đáp án không thuộc câu hỏi.");
            }
            if (chosen.put(answer.questionId(), answer.answerId()) != null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mỗi câu hỏi chỉ được chọn một đáp án.");
            }
        }
        return chosen;
    }

    private Submission createDraft(UUID studentId, UUID exerciseId) {
        Submission draft = Submission.builder().id(UUID.randomUUID()).studentId(studentId).exerciseId(exerciseId)
                .status(SubmissionStatus.DRAFT).build();
        try {
            return submissionRepository.saveAndFlush(draft);
        } catch (DataIntegrityViolationException e) {
            // Hai request lưu tạm đầu tiên cùng lúc, unique (student_id, exercise_id) chặn người đến sau.
            throw new BusinessException(ErrorCode.CONCURRENT_MODIFICATION);
        }
    }

    // Đáp án đúng và kết quả từng câu chỉ lộ sau khi đã nộp; câu hỏi đã bị giảng viên thay thì bỏ qua.
    private SubmissionResponse toResponse(Submission submission, java.util.Collection<SubmissionAnswer> answers,
                                          ExerciseAnswerKey key) {
        boolean submitted = submission.getStatus() == SubmissionStatus.SUBMITTED;
        Map<UUID, SubmissionAnswer> byQuestion = new HashMap<>();
        answers.forEach(a -> byQuestion.put(a.getQuestionId(), a));
        List<SubmissionAnswerResponse> items = key.questions().stream().map(q -> {
            SubmissionAnswer answer = byQuestion.get(q.questionId());
            if (answer == null) {
                return null;
            }
            return new SubmissionAnswerResponse(q.questionId(), answer.getAnswerId(),
                    submitted ? answer.getCorrect() : null, submitted ? q.correctAnswerId() : null);
        }).filter(Objects::nonNull).toList();
        return new SubmissionResponse(submission.getExerciseId(), submission.getStatus(), submission.getScore(),
                submission.getTotalQuestions(), submission.getSubmittedAt(), items);
    }
}
