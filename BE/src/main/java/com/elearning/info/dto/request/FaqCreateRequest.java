package com.elearning.info.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Thêm câu hỏi thường gặp (UC013, Bảng 2-29).
 *
 * @param question nội dung câu hỏi
 * @param answer   nội dung câu trả lời
 */
public record FaqCreateRequest(
        @NotBlank(message = "Câu hỏi không được để trống") String question,
        @NotBlank(message = "Câu trả lời không được để trống") String answer
) {
}
