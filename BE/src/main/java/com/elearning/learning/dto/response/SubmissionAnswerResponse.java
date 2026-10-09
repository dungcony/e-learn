package com.elearning.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Đáp án học viên đã chọn cho một câu hỏi.
 *
 * @param isCorrect       đúng hay sai; null khi bài còn {@code DRAFT}
 * @param correctAnswerId id đáp án đúng; null khi bài còn {@code DRAFT} để không lộ đáp án trước khi nộp
 */
public record SubmissionAnswerResponse(
        UUID questionId,
        UUID answerId,
        // Gắn tên tường minh vì Jackson có thể hiểu isCorrect() của record là getter của thuộc tính "correct".
        @JsonProperty("is_correct") Boolean isCorrect,
        UUID correctAnswerId
) {
}
