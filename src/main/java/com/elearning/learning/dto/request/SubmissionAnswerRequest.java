package com.elearning.learning.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Đáp án học viên chọn cho một câu hỏi.
 *
 * @param questionId id câu hỏi thuộc bài tập
 * @param answerId   id đáp án thuộc câu hỏi đó
 */
public record SubmissionAnswerRequest(
        @NotNull(message = "Cần chỉ rõ câu hỏi") UUID questionId,
        @NotNull(message = "Cần chỉ rõ đáp án") UUID answerId
) {
}
