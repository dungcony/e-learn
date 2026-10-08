package com.elearning.course.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.course.dto.request.CourseCreateRequest;
import com.elearning.course.dto.request.CourseSearchRequest;
import com.elearning.course.dto.request.CourseUpdateRequest;
import com.elearning.course.dto.response.CourseDetailResponse;
import com.elearning.course.dto.response.CourseSummaryResponse;
import com.elearning.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Khóa học: Khách và học viên tìm, xem (UC007); giảng viên thêm, sửa, xóa (UC009). {@code GET /courses} và
 * {@code GET /courses/{id}} được mở công khai trong {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<PageResponse<CourseSummaryResponse>> searchPublicCourses(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(name = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            PageRequestParams params) {
        CourseSearchRequest search = new CourseSearchRequest(code, name, price, startDate, endDate, null);
        return ApiResponse.of(PageResponse.of(courseService.searchPublicCourses(search, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseDetailResponse> getCourse(@PathVariable UUID id) {
        return ApiResponse.of(courseService.getCourse(
                SecurityContextUtil.currentUserIdOrNull(), SecurityContextUtil.hasRole("ADMIN"), id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<CourseDetailResponse> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        return ApiResponse.of(courseService.createCourse(SecurityContextUtil.currentUserId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<CourseDetailResponse> updateCourse(
            @PathVariable UUID id, @Valid @RequestBody CourseUpdateRequest request) {
        return ApiResponse.of(courseService.updateCourse(SecurityContextUtil.currentUserId(), id, request));
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<CourseDetailResponse> updateCourseImage(
            @PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ApiResponse.of(courseService.updateCourseImage(SecurityContextUtil.currentUserId(), id, file));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(SecurityContextUtil.currentUserId(), id);
        return ApiResponse.of(null);
    }
}
