package com.elearning.learning.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.learning.dto.request.CommentCreateRequest;
import com.elearning.learning.dto.request.CommentUpdateRequest;
import com.elearning.learning.dto.response.CommentResponse;
import com.elearning.learning.service.CommentService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Học viên thảo luận dưới bài giảng (UC016).
 */
@RestController
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/lectures/{lectureId}/comments")
    public ApiResponse<PageResponse<CommentResponse>> getComments(@PathVariable UUID lectureId, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(
                commentService.getComments(SecurityContextUtil.currentUserId(), lectureId, params)));
    }

    @PostMapping("/lectures/{lectureId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createComment(
            @PathVariable UUID lectureId, @Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.of(commentService.createComment(SecurityContextUtil.currentUserId(), lectureId, request));
    }

    @PutMapping("/comments/{id}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable UUID id, @Valid @RequestBody CommentUpdateRequest request) {
        return ApiResponse.of(commentService.updateComment(SecurityContextUtil.currentUserId(), id, request));
    }

    @DeleteMapping("/comments/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(SecurityContextUtil.currentUserId(), id);
        return ApiResponse.of(null);
    }
}
