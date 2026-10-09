package com.elearning.info.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Sửa câu hỏi thường gặp (UC013). Gửi đủ câu hỏi và câu trả lời.
 *
 * @param question nội dung câu hỏi
 * @param answer   nội dung câu trả lời
 */
public record FaqUpdateRequest(
        @NotBlank(message = "Câu hỏi không được để trống") String question,
        @NotBlank(message = "Câu trả lời không được để trống") String answer
) {
}
