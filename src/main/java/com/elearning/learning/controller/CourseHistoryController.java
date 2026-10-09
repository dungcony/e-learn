package com.elearning.learning.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.learning.dto.response.CourseHistoryResponse;
import com.elearning.learning.dto.response.EnrolledStudentResponse;
import com.elearning.learning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Giảng viên và Quản trị viên xem lịch sử khóa học và học viên của từng khóa (UC014). Giảng viên chỉ thấy khóa học
 * của mình.
 */
@RestController
@RequestMapping("/course-histories")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
@RequiredArgsConstructor
public class CourseHistoryController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public ApiResponse<PageResponse<CourseHistoryResponse>> getCourseHistories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(enrollmentService.getCourseHistories(
                SecurityContextUtil.currentUserId(), SecurityContextUtil.hasRole("ADMIN"), name, code, params)));
    }

    @GetMapping("/{courseId}/students")
    public ApiResponse<PageResponse<EnrolledStudentResponse>> getEnrolledStudents(
            @PathVariable UUID courseId, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(enrollmentService.getEnrolledStudents(
                SecurityContextUtil.currentUserId(), SecurityContextUtil.hasRole("ADMIN"), courseId, params)));
    }
}
