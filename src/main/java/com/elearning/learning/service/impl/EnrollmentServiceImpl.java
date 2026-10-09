package com.elearning.learning.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
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
import com.elearning.learning.service.EnrollmentService;
import com.elearning.user.dto.response.UserBriefResponse;
import com.elearning.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Map<String, String> SORTABLE_FIELDS = Map.of("enrolled_at", "enrolledAt");

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final ProgressCalculator progressCalculator;
    private final LearningMapper learningMapper;

    @Override
    @Transactional
    public EnrollmentResponse enroll(UUID studentId, UUID courseId) {
        CourseBriefResponse course = courseService.getCourseBrief(courseId);
        if (course.status() != CourseStatus.PUBLIC) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học.");
        }
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessException(ErrorCode.ENROLLMENT_ALREADY_EXISTS);
        }
        Enrollment enrollment = Enrollment.builder()
                .id(UUID.randomUUID()).studentId(studentId).courseId(courseId).enrolledAt(Instant.now()).build();
        try {
            enrollmentRepository.saveAndFlush(enrollment);
        } catch (DataIntegrityViolationException e) {
            // Hai request cùng ghi danh chạy song song cùng qua bước kiểm tra, unique constraint chặn người đến sau.
            throw new BusinessException(ErrorCode.ENROLLMENT_ALREADY_EXISTS);
        }
        return learningMapper.toEnrollmentResponse(enrollment, course, teacherNames(List.of(course.teacherId())).get(course.teacherId()), 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getMyCourses(UUID studentId, String name, String code, PageRequestParams params) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        Map<UUID, CourseBriefResponse> courses = courseService
                .getCourseBriefs(enrollments.stream().map(Enrollment::getCourseId).toList()).stream()
                .filter(c -> c.status() == CourseStatus.PUBLIC)
                .collect(Collectors.toMap(CourseBriefResponse::id, Function.identity()));

        // Số khóa học một học viên ghi danh nhỏ nên lọc và phân trang trong bộ nhớ, vì tên và mã khóa học nằm ở module khác.
        List<Enrollment> matching = enrollments.stream()
                .filter(e -> courses.containsKey(e.getCourseId()))
                .filter(e -> contains(courses.get(e.getCourseId()).title(), name))
                .filter(e -> contains(courses.get(e.getCourseId()).code(), code))
                .sorted(comparator(params, courses))
                .toList();
        int from = Math.min((params.page() - 1) * params.pageSize(), matching.size());
        List<Enrollment> pageItems = matching.subList(from, Math.min(from + params.pageSize(), matching.size()));

        List<UUID> courseIds = pageItems.stream().map(Enrollment::getCourseId).toList();
        Map<UUID, Integer> progress = progressCalculator.forStudent(studentId, courseIds);
        Map<UUID, String> teachers = teacherNames(pageItems.stream().map(e -> courses.get(e.getCourseId()).teacherId()).toList());
        List<EnrollmentResponse> content = pageItems.stream().map(e -> {
            CourseBriefResponse course = courses.get(e.getCourseId());
            return learningMapper.toEnrollmentResponse(e, course, teachers.get(course.teacherId()),
                    progress.getOrDefault(course.id(), 0));
        }).toList();
        return new PageImpl<>(content, PageRequest.of(params.page() - 1, params.pageSize()), matching.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseHistoryResponse> getCourseHistories(UUID viewerId, boolean viewerIsAdmin, String name, String code,
                                                          PageRequestParams params) {
        Page<CourseBriefResponse> courses = courseService.searchCourseBriefs(viewerIsAdmin ? null : viewerId, name, code, params);
        Map<UUID, Long> counts = enrollmentRepository
                .countByCourseIds(courses.stream().map(CourseBriefResponse::id).toList()).stream()
                .collect(Collectors.toMap(CourseStudentCount::getCourseId, CourseStudentCount::getStudentCount));
        return courses.map(c -> learningMapper.toCourseHistoryResponse(c, counts.getOrDefault(c.id(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrolledStudentResponse> getEnrolledStudents(UUID viewerId, boolean viewerIsAdmin, UUID courseId,
                                                             PageRequestParams params) {
        CourseBriefResponse course = courseService.getCourseBrief(courseId);
        if (!viewerIsAdmin && !course.teacherId().equals(viewerId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học.");
        }
        Page<Enrollment> page = enrollmentRepository.findByCourseId(courseId, params.toPageable(SORTABLE_FIELDS, "enrolledAt"));
        List<UUID> studentIds = page.stream().map(Enrollment::getStudentId).toList();
        Map<UUID, UserBriefResponse> users = userService.getUserBriefs(studentIds).stream()
                .collect(Collectors.toMap(UserBriefResponse::id, Function.identity()));
        Map<UUID, Integer> progress = progressCalculator.forCourse(courseId, studentIds);
        return page.map(e -> learningMapper.toEnrolledStudentResponse(
                e, users.get(e.getStudentId()), progress.getOrDefault(e.getStudentId(), 0)));
    }

    private Map<UUID, String> teacherNames(Collection<UUID> teacherIds) {
        Map<UUID, String> names = new HashMap<>();
        userService.getUserBriefs(teacherIds.stream().distinct().toList()).forEach(b -> names.put(b.id(), b.fullName()));
        return names;
    }

    private boolean contains(String value, String query) {
        return !StringUtils.hasText(query)
                || value.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
    }

    private Comparator<Enrollment> comparator(PageRequestParams params, Map<UUID, CourseBriefResponse> courses) {
        Comparator<Enrollment> comparator = "title".equals(params.sortBy())
                ? Comparator.comparing(e -> courses.get(e.getCourseId()).title(), String.CASE_INSENSITIVE_ORDER)
                : Comparator.comparing(Enrollment::getEnrolledAt);
        comparator = comparator.thenComparing(Enrollment::getId);
        return "asc".equals(params.sortOrder()) ? comparator : comparator.reversed();
    }
}
