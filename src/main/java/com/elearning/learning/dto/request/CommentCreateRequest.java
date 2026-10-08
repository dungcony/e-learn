package com.elearning.learning.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Đăng bình luận dưới bài giảng (UC016 bước 12).
 *
 * @param content  nội dung
 * @param parentId bình luận gốc đang trả lời; bỏ trống nếu là bình luận mới. Chỉ trả lời một cấp
 */
public record CommentCreateRequest(
        @NotBlank(message = "Nội dung bình luận không được để trống") String content,
        UUID parentId
) {
}
