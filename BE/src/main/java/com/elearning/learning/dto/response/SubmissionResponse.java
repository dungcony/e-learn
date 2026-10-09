package com.elearning.learning.dto.response;

import com.elearning.learning.enums.SubmissionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Bài làm của học viên.
 *
 * @param score          số câu đúng; null khi chưa nộp
 * @param totalQuestions số câu của bài tập lúc nộp; null khi chưa nộp
 * @param submittedAt    thời điểm nộp; null khi chưa nộp
 * @param answers        đáp án đã chọn, theo thứ tự câu hỏi
 */
public record SubmissionResponse(
        UUID exerciseId,
        SubmissionStatus status,
        Integer score,
        Integer totalQuestions,
        Instant submittedAt,
        List<SubmissionAnswerResponse> answers
) {
}
