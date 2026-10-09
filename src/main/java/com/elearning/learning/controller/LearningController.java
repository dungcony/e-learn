package com.elearning.learning.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.learning.dto.request.SubmissionSaveRequest;
import com.elearning.learning.dto.request.SubmissionStatusUpdateRequest;
import com.elearning.learning.dto.response.MyLectureDetailResponse;
import com.elearning.learning.dto.response.MyLectureSummaryResponse;
import com.elearning.learning.dto.response.SubmissionResponse;
import com.elearning.learning.enums.SubmissionStatus;
import com.elearning.learning.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Học viên học bài giảng, làm và nộp bài tập (UC016).
 */
@RestController
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @GetMapping("/my-courses/{courseId}/lectures")
    public ApiResponse<List<MyLectureSummaryResponse>> getMyLectures(@PathVariable UUID courseId) {
        return ApiResponse.of(learningService.getMyLectures(SecurityContextUtil.currentUserId(), courseId));
    }

    @GetMapping("/my-courses/{courseId}/lectures/{id}")
    public ApiResponse<MyLectureDetailResponse> getMyLecture(@PathVariable UUID courseId, @PathVariable UUID id) {
        return ApiResponse.of(learningService.getMyLecture(SecurityContextUtil.currentUserId(), courseId, id));
    }

    @PutMapping("/lectures/{lectureId}/completion")
    public ApiResponse<Void> completeLecture(@PathVariable UUID lectureId) {
        learningService.completeLecture(SecurityContextUtil.currentUserId(), lectureId);
        return ApiResponse.of(null);
    }

    @PutMapping("/exercises/{exerciseId}/submission")
    public ApiResponse<SubmissionResponse> saveSubmission(
            @PathVariable UUID exerciseId, @Valid @RequestBody SubmissionSaveRequest request) {
        return ApiResponse.of(learningService.saveSubmission(SecurityContextUtil.currentUserId(), exerciseId, request));
    }

    @PutMapping("/exercises/{exerciseId}/submission/status")
    public ApiResponse<SubmissionResponse> updateSubmissionStatus(
            @PathVariable UUID exerciseId, @Valid @RequestBody SubmissionStatusUpdateRequest request) {
        if (request.status() != SubmissionStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Trạng thái chỉ được là SUBMITTED.");
        }
        return ApiResponse.of(learningService.submit(SecurityContextUtil.currentUserId(), exerciseId));
    }

    @GetMapping("/exercises/{exerciseId}/submission")
    public ApiResponse<SubmissionResponse> getSubmission(@PathVariable UUID exerciseId) {
        return ApiResponse.of(learningService.getSubmission(SecurityContextUtil.currentUserId(), exerciseId));
    }
}
