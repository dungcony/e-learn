package com.elearning.course.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Đáp án chuẩn của một câu hỏi để chấm bài.
 *
 * @param answerIds       id các đáp án của câu hỏi, dùng kiểm tra đáp án học viên chọn có thuộc câu hỏi không
 * @param correctAnswerId id đáp án đúng
 */
public record QuestionAnswerKey(UUID questionId, List<UUID> answerIds, UUID correctAnswerId) {
}
