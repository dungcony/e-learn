package com.elearning.course.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.course.dto.request.CourseSearchRequest;
import com.elearning.course.dto.response.CourseSummaryResponse;
import com.elearning.course.enums.CourseStatus;
import com.elearning.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Danh sách khóa học của chính giảng viên đang đăng nhập, gồm cả khóa {@code PRIVATE} (UC009).
 */
@RestController
@RequestMapping("/teachers/me/courses")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<PageResponse<CourseSummaryResponse>> searchMyCourses(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(name = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) CourseStatus status,
            PageRequestParams params) {
        CourseSearchRequest search = new CourseSearchRequest(code, name, price, startDate, endDate, status);
        return ApiResponse.of(PageResponse.of(
                courseService.searchTeacherCourses(SecurityContextUtil.currentUserId(), search, params)));
    }
}
