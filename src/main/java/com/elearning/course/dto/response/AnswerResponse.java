package com.elearning.course.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Đáp án kèm cờ đúng/sai, chỉ trả cho giảng viên chủ khóa học.
 */
public record AnswerResponse(
        UUID id,
        String content,
        // Gắn tên tường minh vì Jackson có thể hiểu isCorrect() của record là getter của thuộc tính "correct".
        @JsonProperty("is_correct") boolean isCorrect
) {
}
