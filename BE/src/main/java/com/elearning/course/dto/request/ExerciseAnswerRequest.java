package com.elearning.course.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Một đáp án của câu hỏi.
 *
 * @param content   nội dung đáp án
 * @param isCorrect {@code true} nếu là đáp án đúng; mỗi câu hỏi có đúng một đáp án đúng
 */
public record ExerciseAnswerRequest(
        @NotBlank(message = "Nội dung đáp án không được để trống") String content,
        // Gắn tên tường minh vì Jackson có thể hiểu isCorrect() của record là getter của thuộc tính "correct".
        @JsonProperty("is_correct") @NotNull(message = "Cần đánh dấu đáp án đúng hoặc sai") Boolean isCorrect
) {
}
