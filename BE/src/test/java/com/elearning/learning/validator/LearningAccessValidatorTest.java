package com.elearning.learning.validator;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.course.dto.response.CourseBriefResponse;
import com.elearning.course.enums.CourseStatus;
import com.elearning.course.service.CourseService;
import com.elearning.learning.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningAccessValidatorTest {

    // 12:00 giờ Việt Nam ngày 2026-10-08.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-08T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 8);

    @Mock
    private CourseService courseService;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    private LearningAccessValidator validator;

    private final UUID studentId = UUID.randomUUID();
    private final UUID courseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validator = new LearningAccessValidator(courseService, enrollmentRepository, CLOCK);
    }

    @Test
    void requireLearningAccess_enrolledStudentOfStartedPublicCourseGetsTheCourse() {
        CourseBriefResponse course = course(CourseStatus.PUBLIC, TODAY.minusDays(1));
        when(courseService.getCourseBrief(courseId)).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        assertThat(validator.requireLearningAccess(studentId, courseId)).isSameAs(course);
    }

    @Test
    void requireLearningAccess_courseStartingTodayIsAlreadyOpen() {
        when(courseService.getCourseBrief(courseId)).thenReturn(course(CourseStatus.PUBLIC, TODAY));
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        assertThat(validator.requireLearningAccess(studentId, courseId)).isNotNull();
    }

    @Test
    void requireLearningAccess_privateCourseIsHiddenEvenFromEnrolledStudent() {
        when(courseService.getCourseBrief(courseId)).thenReturn(course(CourseStatus.PRIVATE, TODAY.minusDays(1)));

        assertCode(ErrorCode.NOT_FOUND);
        verifyNoInteractions(enrollmentRepository);
    }

    @Test
    void requireLearningAccess_notEnrolledIsRejected() {
        when(courseService.getCourseBrief(courseId)).thenReturn(course(CourseStatus.PUBLIC, TODAY.minusDays(1)));
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);

        assertCode(ErrorCode.ENROLLMENT_REQUIRED);
    }

    @Test
    void requireLearningAccess_courseStartingTomorrowIsNotStarted() {
        when(courseService.getCourseBrief(courseId)).thenReturn(course(CourseStatus.PUBLIC, TODAY.plusDays(1)));
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        assertCode(ErrorCode.COURSE_NOT_STARTED);
    }

    @Test
    void requireLearningAccess_enrollmentIsCheckedBeforeStartDate() {
        when(courseService.getCourseBrief(courseId)).thenReturn(course(CourseStatus.PUBLIC, TODAY.plusDays(30)));
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);

        assertCode(ErrorCode.ENROLLMENT_REQUIRED);
    }

    @Test
    void requireLearningAccess_missingCourseNotFoundPropagates() {
        when(courseService.getCourseBrief(courseId)).thenThrow(new BusinessException(ErrorCode.NOT_FOUND));

        assertCode(ErrorCode.NOT_FOUND);
    }

    private void assertCode(ErrorCode expected) {
        assertThatThrownBy(() -> validator.requireLearningAccess(studentId, courseId))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo(expected.name()));
    }

    private CourseBriefResponse course(CourseStatus status, LocalDate startDate) {
        return new CourseBriefResponse(courseId, "CO123456", "Java", status, startDate, startDate.plusDays(60), null, UUID.randomUUID());
    }
}
