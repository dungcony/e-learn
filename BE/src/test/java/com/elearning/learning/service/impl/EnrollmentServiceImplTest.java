package com.elearning.learning.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.response.PageRequestParams;
import com.elearning.course.dto.response.CourseBriefResponse;
import com.elearning.course.enums.CourseStatus;
import com.elearning.course.service.CourseService;
import com.elearning.learning.dto.response.CourseHistoryResponse;
import com.elearning.learning.dto.response.EnrolledStudentResponse;
import com.elearning.learning.dto.response.EnrollmentResponse;
import com.elearning.learning.entity.Enrollment;
import com.elearning.learning.helper.ProgressCalculator;
import com.elearning.learning.mapper.LearningMapper;
import com.elearning.learning.repository.CourseStudentCount;
import com.elearning.learning.repository.EnrollmentRepository;
import com.elearning.user.dto.response.UserBriefResponse;
import com.elearning.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private UserService userService;
    @Mock
    private ProgressCalculator progressCalculator;

    private EnrollmentServiceImpl service;

    private final UUID studentId = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new EnrollmentServiceImpl(enrollmentRepository, courseService, userService, progressCalculator,
                Mappers.getMapper(LearningMapper.class));
    }

    // ---- enroll ----

    @Test
    void enroll_publicCourseCreatesEnrollmentWithZeroProgress() {
        CourseBriefResponse course = course("Java", "CO000001", CourseStatus.PUBLIC);
        when(courseService.getCourseBrief(course.id())).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, course.id())).thenReturn(false);
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of(new UserBriefResponse(teacherId, "GV Một", "gv@x.y")));

        EnrollmentResponse response = service.enroll(studentId, course.id());

        verify(enrollmentRepository).saveAndFlush(any(Enrollment.class));
        assertThat(response.courseId()).isEqualTo(course.id());
        assertThat(response.progress()).isZero();
        assertThat(response.teacherName()).isEqualTo("GV Một");
    }

    @Test
    void enroll_privateCourseIsNotFoundAndNothingIsSaved() {
        CourseBriefResponse course = course("Java", "CO000001", CourseStatus.PRIVATE);
        when(courseService.getCourseBrief(course.id())).thenReturn(course);

        assertThatThrownBy(() -> service.enroll(studentId, course.id()))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        verify(enrollmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void enroll_alreadyEnrolledConflicts() {
        CourseBriefResponse course = course("Java", "CO000001", CourseStatus.PUBLIC);
        when(courseService.getCourseBrief(course.id())).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, course.id())).thenReturn(true);

        assertThatThrownBy(() -> service.enroll(studentId, course.id()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("ENROLLMENT_ALREADY_EXISTS"));
        verify(enrollmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void enroll_uniqueConstraintRaceMapsToAlreadyEnrolled() {
        CourseBriefResponse course = course("Java", "CO000001", CourseStatus.PUBLIC);
        when(courseService.getCourseBrief(course.id())).thenReturn(course);
        when(enrollmentRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq_enrollments"));

        assertThatThrownBy(() -> service.enroll(studentId, course.id()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("ENROLLMENT_ALREADY_EXISTS"));
    }

    // ---- getMyCourses ----

    @Test
    void getMyCourses_hidesPrivateAndDeletedCoursesAndFiltersByNameAndCode() {
        CourseBriefResponse java = course("Java cơ bản", "CO111111", CourseStatus.PUBLIC);
        CourseBriefResponse python = course("Python", "CO222222", CourseStatus.PUBLIC);
        CourseBriefResponse hidden = course("Ẩn", "CO333333", CourseStatus.PRIVATE);
        UUID deletedCourse = UUID.randomUUID();
        List<Enrollment> enrollments = List.of(enrollment(java, 1), enrollment(python, 2), enrollment(hidden, 3),
                enrollment(deletedCourse, 4));
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(enrollments);
        when(courseService.getCourseBriefs(anyCollection())).thenReturn(List.of(java, python, hidden));
        when(progressCalculator.forStudent(eq(studentId), anyCollection())).thenReturn(Map.of(java.id(), 40, python.id(), 0));
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of());

        Page<EnrollmentResponse> all = service.getMyCourses(studentId, null, null, PageRequestParams.of(null, null, null, null));
        Page<EnrollmentResponse> byName = service.getMyCourses(studentId, "JAVA", null, PageRequestParams.of(null, null, null, null));
        Page<EnrollmentResponse> byCode = service.getMyCourses(studentId, null, "222", PageRequestParams.of(null, null, null, null));

        assertThat(all.getContent()).extracting(EnrollmentResponse::title).containsExactly("Python", "Java cơ bản");
        assertThat(byName.getContent()).extracting(EnrollmentResponse::title).containsExactly("Java cơ bản");
        assertThat(byName.getContent().get(0).progress()).isEqualTo(40);
        assertThat(byCode.getContent()).extracting(EnrollmentResponse::title).containsExactly("Python");
    }

    @Test
    void getMyCourses_sortsByTitleAndPaginatesInMemory() {
        CourseBriefResponse a = course("Alpha", "CO000001", CourseStatus.PUBLIC);
        CourseBriefResponse b = course("beta", "CO000002", CourseStatus.PUBLIC);
        CourseBriefResponse c = course("Gamma", "CO000003", CourseStatus.PUBLIC);
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(List.of(enrollment(c, 1), enrollment(a, 2), enrollment(b, 3)));
        when(courseService.getCourseBriefs(anyCollection())).thenReturn(List.of(a, b, c));
        when(progressCalculator.forStudent(eq(studentId), anyCollection())).thenReturn(Map.of());
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of());

        Page<EnrollmentResponse> page2 = service.getMyCourses(studentId, null, null, PageRequestParams.of(2, 2, "title", "asc"));

        assertThat(page2.getContent()).extracting(EnrollmentResponse::title).containsExactly("Gamma");
        assertThat(page2.getTotalElements()).isEqualTo(3);
        assertThat(page2.getTotalPages()).isEqualTo(2);
        assertThat(page2.getNumber()).isEqualTo(1);
    }

    @Test
    void getMyCourses_pageBeyondTheEndIsEmptyNotAnError() {
        CourseBriefResponse a = course("Alpha", "CO000001", CourseStatus.PUBLIC);
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(List.of(enrollment(a, 1)));
        when(courseService.getCourseBriefs(anyCollection())).thenReturn(List.of(a));
        when(progressCalculator.forStudent(eq(studentId), anyCollection())).thenReturn(Map.of());
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of());

        Page<EnrollmentResponse> page = service.getMyCourses(studentId, null, null, PageRequestParams.of(5, 10, null, null));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    // ---- UC014 ----

    @Test
    void getCourseHistories_adminSeesAllTeachersTeacherSeesOwn() {
        CourseBriefResponse course = course("Java", "CO111111", CourseStatus.PRIVATE);
        PageRequestParams params = PageRequestParams.of(null, null, null, null);
        when(courseService.searchCourseBriefs(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(course)));
        when(enrollmentRepository.countByCourseIds(anyCollection())).thenReturn(List.of(count(course.id(), 7)));

        Page<CourseHistoryResponse> asAdmin = service.getCourseHistories(teacherId, true, "j", "c", params);
        service.getCourseHistories(teacherId, false, null, null, params);

        verify(courseService).searchCourseBriefs(null, "j", "c", params);
        verify(courseService).searchCourseBriefs(teacherId, null, null, params);
        assertThat(asAdmin.getContent().get(0).studentCount()).isEqualTo(7);
        assertThat(asAdmin.getContent().get(0).status()).isEqualTo(CourseStatus.PRIVATE);
    }

    @Test
    void getCourseHistories_courseWithoutEnrollmentsHasZeroStudents() {
        CourseBriefResponse course = course("Java", "CO111111", CourseStatus.PUBLIC);
        when(courseService.searchCourseBriefs(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(course)));
        when(enrollmentRepository.countByCourseIds(anyCollection())).thenReturn(List.of());

        Page<CourseHistoryResponse> page = service.getCourseHistories(teacherId, false, null, null, PageRequestParams.of(null, null, null, null));

        assertThat(page.getContent().get(0).studentCount()).isZero();
    }

    @Test
    void getEnrolledStudents_teacherOfAnotherCourseIsNotFound() {
        CourseBriefResponse course = course("Java", "CO111111", CourseStatus.PUBLIC);
        when(courseService.getCourseBrief(course.id())).thenReturn(course);

        assertThatThrownBy(() -> service.getEnrolledStudents(UUID.randomUUID(), false, course.id(), PageRequestParams.of(null, null, null, null)))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        verify(enrollmentRepository, never()).findByCourseId(any(), any());
    }

    @Test
    void getEnrolledStudents_ownerAndAdminSeeStudentsWithNamesAndProgress() {
        CourseBriefResponse course = course("Java", "CO111111", CourseStatus.PUBLIC);
        Enrollment known = enrollment(course, 1);
        Enrollment unknownUser = Enrollment.builder().id(UUID.randomUUID()).studentId(UUID.randomUUID())
                .courseId(course.id()).enrolledAt(Instant.parse("2026-01-02T00:00:00Z")).build();
        when(courseService.getCourseBrief(course.id())).thenReturn(course);
        when(enrollmentRepository.findByCourseId(eq(course.id()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(known, unknownUser)));
        when(userService.getUserBriefs(anyCollection())).thenReturn(List.of(new UserBriefResponse(studentId, "Học viên", "hv@x.y")));
        when(progressCalculator.forCourse(eq(course.id()), anyCollection())).thenReturn(Map.of(studentId, 50));

        Page<EnrolledStudentResponse> asOwner = service.getEnrolledStudents(teacherId, false, course.id(), PageRequestParams.of(null, null, null, null));
        Page<EnrolledStudentResponse> asAdmin = service.getEnrolledStudents(UUID.randomUUID(), true, course.id(), PageRequestParams.of(null, null, null, null));

        assertThat(asOwner.getContent().get(0)).extracting(EnrolledStudentResponse::fullName, EnrolledStudentResponse::email,
                EnrolledStudentResponse::progress).containsExactly("Học viên", "hv@x.y", 50);
        assertThat(asOwner.getContent().get(1).fullName()).isNull();
        assertThat(asOwner.getContent().get(1).progress()).isZero();
        assertThat(asAdmin.getContent()).hasSize(2);
    }

    private CourseBriefResponse course(String title, String code, CourseStatus status) {
        return new CourseBriefResponse(UUID.randomUUID(), code, title, status, LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 31), null, teacherId);
    }

    private Enrollment enrollment(CourseBriefResponse course, int dayOfMonth) {
        return enrollment(course.id(), dayOfMonth);
    }

    private Enrollment enrollment(UUID courseId, int dayOfMonth) {
        return Enrollment.builder().id(UUID.randomUUID()).studentId(studentId).courseId(courseId)
                .enrolledAt(Instant.parse("2026-01-%02dT00:00:00Z".formatted(dayOfMonth))).build();
    }

    private CourseStudentCount count(UUID courseId, long students) {
        return new CourseStudentCount() {
            public UUID getCourseId() { return courseId; }
            public long getStudentCount() { return students; }
        };
    }
}
