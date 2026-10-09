package com.elearning.learning.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Sửa bình luận của chính mình.
 *
 * @param content nội dung mới
 */
public record CommentUpdateRequest(
        @NotBlank(message = "Nội dung bình luận không được để trống") String content
) {
}
