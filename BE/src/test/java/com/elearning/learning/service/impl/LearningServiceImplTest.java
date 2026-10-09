package com.elearning.learning.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.course.dto.response.ExerciseAnswerKey;
import com.elearning.course.dto.response.LearningAnswerResponse;
import com.elearning.course.dto.response.LearningExerciseResponse;
import com.elearning.course.dto.response.LearningLectureResponse;
import com.elearning.course.dto.response.LearningQuestionResponse;
import com.elearning.course.dto.response.QuestionAnswerKey;
import com.elearning.course.service.LectureService;
import com.elearning.learning.dto.request.SubmissionAnswerRequest;
import com.elearning.learning.dto.request.SubmissionSaveRequest;
import com.elearning.learning.dto.response.MyLectureDetailResponse;
import com.elearning.learning.dto.response.SubmissionResponse;
import com.elearning.learning.entity.Submission;
import com.elearning.learning.entity.SubmissionAnswer;
import com.elearning.learning.enums.SubmissionStatus;
import com.elearning.learning.repository.LectureCompletionRepository;
import com.elearning.learning.repository.SubmissionAnswerRepository;
import com.elearning.learning.repository.SubmissionRepository;
import com.elearning.learning.validator.LearningAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningServiceImplTest {

    @Mock
    private LearningAccessValidator accessValidator;
    @Mock
    private LectureService lectureService;
    @Mock
    private LectureCompletionRepository completionRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private SubmissionAnswerRepository submissionAnswerRepository;

    private LearningServiceImpl service;

    private final UUID studentId = UUID.randomUUID();
    private final UUID courseId = UUID.randomUUID();
    private final UUID lectureId = UUID.randomUUID();
    private final UUID exerciseId = UUID.randomUUID();

    // Hai câu hỏi, mỗi câu 2 đáp án; đáp án đầu là đáp án đúng.
    private final UUID q1 = UUID.randomUUID();
    private final UUID q1Right = UUID.randomUUID();
    private final UUID q1Wrong = UUID.randomUUID();
    private final UUID q2 = UUID.randomUUID();
    private final UUID q2Right = UUID.randomUUID();
    private final UUID q2Wrong = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new LearningServiceImpl(accessValidator, lectureService, completionRepository, submissionRepository,
                submissionAnswerRepository);
        lenient().when(lectureService.getExerciseAnswerKey(exerciseId)).thenReturn(key());
    }

    // ---- lưu tạm ----

    @Test
    void saveSubmission_firstSaveCreatesDraftAndStoresAnswersWithoutRevealingResult() {
        when(submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId)).thenReturn(Optional.empty());
        when(submissionRepository.saveAndFlush(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));
        when(submissionAnswerRepository.findBySubmissionId(any())).thenReturn(List.of());

        SubmissionResponse response = service.saveSubmission(studentId, exerciseId, save(q1, q1Wrong));

        verify(accessValidator).requireLearningAccess(studentId, courseId);
        ArgumentCaptor<List<SubmissionAnswer>> inserted = ArgumentCaptor.forClass(List.class);
        verify(submissionAnswerRepository).saveAll(inserted.capture());
        assertThat(inserted.getValue()).extracting(SubmissionAnswer::getAnswerId).containsExactly(q1Wrong);
        assertThat(response.status()).isEqualTo(SubmissionStatus.DRAFT);
        assertThat(response.score()).isNull();
        assertThat(response.answers()).hasSize(1);
        assertThat(response.answers().get(0).isCorrect()).isNull();
        assertThat(response.answers().get(0).correctAnswerId()).isNull();
    }

    @Test
    void saveSubmission_overwritesExistingAnswerInPlaceAndInsertsOnlyNewQuestions() {
        Submission draft = submission(SubmissionStatus.DRAFT);
        SubmissionAnswer existing = answer(draft, q1, q1Wrong);
        when(submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId)).thenReturn(Optional.of(draft));
        when(submissionAnswerRepository.findBySubmissionId(draft.getId())).thenReturn(List.of(existing));

        SubmissionResponse response = service.saveSubmission(studentId, exerciseId,
                new SubmissionSaveRequest(List.of(new SubmissionAnswerRequest(q1, q1Right), new SubmissionAnswerRequest(q2, q2Wrong))));

        assertThat(existing.getAnswerId()).isEqualTo(q1Right);
        ArgumentCaptor<List<SubmissionAnswer>> inserted = ArgumentCaptor.forClass(List.class);
        verify(submissionAnswerRepository).saveAll(inserted.capture());
        assertThat(inserted.getValue()).extracting(SubmissionAnswer::getQuestionId).containsExactly(q2);
        assertThat(response.answers()).hasSize(2);
    }

    @Test
    void saveSubmission_rejectsQuestionFromAnotherExercise() {
        assertValidationError(() -> service.saveSubmission(studentId, exerciseId, save(UUID.randomUUID(), q1Right)));
        verify(submissionRepository, never()).saveAndFlush(any());
    }

    @Test
    void saveSubmission_rejectsAnswerBelongingToAnotherQuestion() {
        assertValidationError(() -> service.saveSubmission(studentId, exerciseId, save(q1, q2Right)));
    }

    @Test
    void saveSubmission_rejectsTwoAnswersForTheSameQuestion() {
        assertValidationError(() -> service.saveSubmission(studentId, exerciseId, new SubmissionSaveRequest(List.of(
                new SubmissionAnswerRequest(q1, q1Right), new SubmissionAnswerRequest(q1, q1Wrong)))));
    }

    @Test
    void saveSubmission_afterSubmittedConflicts() {
        when(submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId))
                .thenReturn(Optional.of(submission(SubmissionStatus.SUBMITTED)));

        assertThatThrownBy(() -> service.saveSubmission(studentId, exerciseId, save(q1, q1Right)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("SUBMISSION_ALREADY_SUBMITTED"));
        verify(submissionAnswerRepository, never()).saveAll(anyCollection());
    }

    // ---- nộp bài ----

    @Test
    void submit_scoresEachQuestionStoresResultAndRevealsCorrectAnswers() {
        Submission draft = submission(SubmissionStatus.DRAFT);
        List<SubmissionAnswer> answers = List.of(answer(draft, q1, q1Right), answer(draft, q2, q2Wrong));
        when(submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId)).thenReturn(Optional.of(draft));
        when(submissionAnswerRepository.findBySubmissionId(draft.getId())).thenReturn(answers);

        SubmissionResponse response = service.submit(studentId, exerciseId);

        assertThat(draft.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(draft.getScore()).isEqualTo(1);
        assertThat(draft.getTotalQuestions()).isEqualTo(2);
        assertThat(draft.getSubmittedAt()).isNotNull();
        assertThat(answers).extracting(SubmissionAnswer::getCorrect).containsExactly(true, false);
        assertThat(response.answers()).extracting(a -> a.isCorrect()).containsExactly(true, false);
        assertThat(response.answers()).extracting(a -> a.correctAnswerId()).containsExactly(q1Right, q2Right);
    }

    @Test
    void submit_missingAnswerForAnyCurrentQuestionIsIncompleteAndChangesNothing() {
        Submission draft = submission(SubmissionStatus.DRAFT);
        when(submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId)).thenReturn(Optional.of(draft));
        when(submissionAnswerRepository.findBySubmissionId(draft.getId())).thenReturn(List.of(answer(draft, q1, q1Right)));

        assertThatThrownBy(() -> service.submit(studentId, exerciseId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("SUBMISSION_INCOMPLETE"));
    }

    @Test
    void submit_withoutAnySavedAnswersIsIncomplete() {
        when(submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submit(studentId, exerciseId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("SUBMISSION_INCOMPLETE"));
    }

    @Test
    void submit_secondTimeConflicts() {
        when(submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId))
                .thenReturn(Optional.of(submission(SubmissionStatus.SUBMITTED)));

        assertThatThrownBy(() -> service.submit(studentId, exerciseId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("SUBMISSION_ALREADY_SUBMITTED"));
    }

    @Test
    void submit_answersToQuestionsTheTeacherReplacedAreIgnored() {
        Submission draft = submission(SubmissionStatus.DRAFT);
        SubmissionAnswer stale = answer(draft, UUID.randomUUID(), UUID.randomUUID());
        when(submissionRepository.findWithLockByStudentIdAndExerciseId(studentId, exerciseId)).thenReturn(Optional.of(draft));
        when(submissionAnswerRepository.findBySubmissionId(draft.getId()))
                .thenReturn(List.of(answer(draft, q1, q1Right), answer(draft, q2, q2Right), stale));

        SubmissionResponse response = service.submit(studentId, exerciseId);

        assertThat(draft.getScore()).isEqualTo(2);
        assertThat(draft.getTotalQuestions()).isEqualTo(2);
        assertThat(stale.getCorrect()).isNull();
        assertThat(response.answers()).hasSize(2);
    }

    @Test
    void submit_checksLearningAccessBeforeTouchingTheSubmission() {
        when(accessValidator.requireLearningAccess(studentId, courseId))
                .thenThrow(new BusinessException(com.elearning.common.exception.ErrorCode.ENROLLMENT_REQUIRED));

        assertThatThrownBy(() -> service.submit(studentId, exerciseId)).isInstanceOf(BusinessException.class);
        verify(submissionRepository, never()).findWithLockByStudentIdAndExerciseId(any(), any());
    }

    // ---- xem kết quả, hoàn thành, nội dung bài giảng ----

    @Test
    void getSubmission_noSubmissionYetIsNotFound() {
        when(submissionRepository.findByStudentIdAndExerciseId(studentId, exerciseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSubmission(studentId, exerciseId))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
    }

    @Test
    void completeLecture_checksAccessForTheLecturesCourseThenRecordsIdempotently() {
        when(lectureService.getCourseIdOfLecture(lectureId)).thenReturn(courseId);

        service.completeLecture(studentId, lectureId);

        InOrder order = inOrder(accessValidator, completionRepository);
        order.verify(accessValidator).requireLearningAccess(studentId, courseId);
        order.verify(completionRepository).insertIfAbsent(any(UUID.class), eq(studentId), eq(courseId), eq(lectureId), any(Instant.class));
    }

    @Test
    void getMyLecture_lectureOfAnotherCourseIsNotFound() {
        when(lectureService.getLearningLecture(lectureId)).thenReturn(
                new LearningLectureResponse(lectureId, UUID.randomUUID(), "Bài", null, "https://x.y", List.of()));

        assertThatThrownBy(() -> service.getMyLecture(studentId, courseId, lectureId))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
    }

    @Test
    void getMyLecture_attachesSubmissionStatusAndCompletionToEachExercise() {
        LearningExerciseResponse exercise = new LearningExerciseResponse(exerciseId, "BT", "d", List.of(
                new LearningQuestionResponse(q1, "Câu 1", List.of(new LearningAnswerResponse(q1Right, "A")))));
        when(lectureService.getLearningLecture(lectureId)).thenReturn(
                new LearningLectureResponse(lectureId, courseId, "Bài", "mô tả", "https://x.y", List.of(exercise)));
        when(completionRepository.existsByStudentIdAndLectureId(studentId, lectureId)).thenReturn(true);
        when(submissionRepository.findByStudentIdAndExerciseIdIn(eq(studentId), anyCollection()))
                .thenReturn(List.of(submission(SubmissionStatus.SUBMITTED)));

        MyLectureDetailResponse response = service.getMyLecture(studentId, courseId, lectureId);

        assertThat(response.completed()).isTrue();
        assertThat(response.exercises()).hasSize(1);
        assertThat(response.exercises().get(0).submissionStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(response.exercises().get(0).questions()).isEqualTo(exercise.questions());
    }

    private void assertValidationError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getCode()).isEqualTo("VALIDATION_ERROR"));
    }

    private SubmissionSaveRequest save(UUID questionId, UUID answerId) {
        return new SubmissionSaveRequest(new ArrayList<>(List.of(new SubmissionAnswerRequest(questionId, answerId))));
    }

    private ExerciseAnswerKey key() {
        return new ExerciseAnswerKey(exerciseId, lectureId, courseId, List.of(
                new QuestionAnswerKey(q1, List.of(q1Right, q1Wrong), q1Right),
                new QuestionAnswerKey(q2, List.of(q2Right, q2Wrong), q2Right)));
    }

    private Submission submission(SubmissionStatus status) {
        return Submission.builder().id(UUID.randomUUID()).studentId(studentId).exerciseId(exerciseId).status(status).build();
    }

    private SubmissionAnswer answer(Submission submission, UUID questionId, UUID answerId) {
        return SubmissionAnswer.builder().id(UUID.randomUUID()).submissionId(submission.getId())
                .questionId(questionId).answerId(answerId).build();
    }
}
