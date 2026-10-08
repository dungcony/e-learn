package com.elearning.course.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.course.dto.request.LectureCreateRequest;
import com.elearning.course.dto.request.LectureUpdateRequest;
import com.elearning.course.dto.response.LectureDetailResponse;
import com.elearning.course.dto.response.LectureSummaryResponse;
import com.elearning.course.service.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Giảng viên chủ khóa học quản lý bài giảng (UC011).
 */
@RestController
@RequestMapping("/courses/{courseId}/lectures")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @GetMapping
    public ApiResponse<PageResponse<LectureSummaryResponse>> searchLectures(
            @PathVariable UUID courseId, @RequestParam(required = false) String name, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(
                lectureService.searchLectures(SecurityContextUtil.currentUserId(), courseId, name, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<LectureDetailResponse> getLecture(@PathVariable UUID courseId, @PathVariable UUID id) {
        return ApiResponse.of(lectureService.getLecture(SecurityContextUtil.currentUserId(), courseId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LectureDetailResponse> createLecture(
            @PathVariable UUID courseId, @Valid @RequestBody LectureCreateRequest request) {
        return ApiResponse.of(lectureService.createLecture(SecurityContextUtil.currentUserId(), courseId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<LectureDetailResponse> updateLecture(
            @PathVariable UUID courseId, @PathVariable UUID id, @Valid @RequestBody LectureUpdateRequest request) {
        return ApiResponse.of(lectureService.updateLecture(SecurityContextUtil.currentUserId(), courseId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLecture(@PathVariable UUID courseId, @PathVariable UUID id) {
        lectureService.deleteLecture(SecurityContextUtil.currentUserId(), courseId, id);
        return ApiResponse.of(null);
    }
}
