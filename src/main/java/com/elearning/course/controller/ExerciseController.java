package com.elearning.course.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.course.dto.request.ExerciseCreateRequest;
import com.elearning.course.dto.request.ExerciseUpdateRequest;
import com.elearning.course.dto.response.ExerciseDetailResponse;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Giảng viên chủ khóa học quản lý bài tập trắc nghiệm của bài giảng (UC011). Học viên làm bài qua module {@code learning}.
 */
@RestController
@RequestMapping("/lectures/{lectureId}/exercises")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class ExerciseController {

    private final LectureService lectureService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ExerciseDetailResponse> createExercise(
            @PathVariable UUID lectureId, @Valid @RequestBody ExerciseCreateRequest request) {
        return ApiResponse.of(lectureService.createExercise(SecurityContextUtil.currentUserId(), lectureId, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExerciseDetailResponse> getExercise(@PathVariable UUID lectureId, @PathVariable UUID id) {
        return ApiResponse.of(lectureService.getExercise(SecurityContextUtil.currentUserId(), lectureId, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ExerciseDetailResponse> updateExercise(
            @PathVariable UUID lectureId, @PathVariable UUID id, @Valid @RequestBody ExerciseUpdateRequest request) {
        return ApiResponse.of(lectureService.updateExercise(SecurityContextUtil.currentUserId(), lectureId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteExercise(@PathVariable UUID lectureId, @PathVariable UUID id) {
        lectureService.deleteExercise(SecurityContextUtil.currentUserId(), lectureId, id);
        return ApiResponse.of(null);
    }
}
