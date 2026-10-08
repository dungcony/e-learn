package com.elearning.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Một câu hỏi trắc nghiệm trong bài tập.
 *
 * @param content nội dung câu hỏi
 * @param answers đúng 4 đáp án (Bảng 2-25); số đáp án đúng do {@code ExerciseValidator} kiểm tra
 */
public record ExerciseQuestionRequest(
        @NotBlank(message = "Nội dung câu hỏi không được để trống") String content,
        @NotNull(message = "Câu hỏi cần có đáp án") @Size(min = 4, max = 4, message = "Mỗi câu hỏi phải có đúng 4 đáp án")
        List<@Valid @NotNull(message = "Đáp án không được để trống") ExerciseAnswerRequest> answers
) {
}
