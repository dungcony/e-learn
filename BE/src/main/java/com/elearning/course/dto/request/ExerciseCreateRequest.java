package com.elearning.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Thêm bài tập trắc nghiệm vào bài giảng (UC011, Bảng 2-23 đến 2-25).
 *
 * @param title       tên bài tập, tối đa 255 ký tự
 * @param description mô tả
 * @param questions   danh sách câu hỏi, ít nhất một câu; thứ tự trong danh sách là thứ tự hiển thị
 */
public record ExerciseCreateRequest(
        @NotBlank(message = "Tên bài tập không được để trống") @Size(max = 255, message = "Tên bài tập tối đa 255 ký tự") String title,
        @NotBlank(message = "Mô tả không được để trống") String description,
        @NotEmpty(message = "Bài tập cần ít nhất một câu hỏi") List<@Valid ExerciseQuestionRequest> questions
) {
}
