package com.elearning.learning.dto.request;

import com.elearning.learning.enums.SubmissionStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Nộp bài tập (UC016 bước 8).
 *
 * @param status chỉ chấp nhận {@code SUBMITTED}
 */
public record SubmissionStatusUpdateRequest(
        @NotNull(message = "Trạng thái không được để trống") SubmissionStatus status
) {
}
