package com.elearning.learning.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.learning.dto.response.EnrollmentResponse;
import com.elearning.learning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Học viên ghi danh khóa học và xem các khóa học đã ghi danh (UC016).
 */
@RestController
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EnrollmentResponse> enroll(@PathVariable UUID courseId) {
        return ApiResponse.of(enrollmentService.enroll(SecurityContextUtil.currentUserId(), courseId));
    }

    @GetMapping("/my-courses")
    public ApiResponse<PageResponse<EnrollmentResponse>> getMyCourses(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(
                enrollmentService.getMyCourses(SecurityContextUtil.currentUserId(), name, code, params)));
    }
}
