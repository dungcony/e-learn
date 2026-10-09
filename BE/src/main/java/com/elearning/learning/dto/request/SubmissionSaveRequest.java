package com.elearning.learning.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Lưu tạm đáp án của bài tập (UC016 bước 7). Không cần đủ câu; câu đã có đáp án thì bị ghi đè.
 *
 * @param answers các đáp án đã chọn, mỗi câu hỏi tối đa một đáp án
 */
public record SubmissionSaveRequest(
        @NotEmpty(message = "Cần ít nhất một đáp án") List<@Valid SubmissionAnswerRequest> answers
) {
}
