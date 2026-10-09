package com.elearning.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Sửa bài tập (UC011). Gửi lại toàn bộ bài tập: câu hỏi và đáp án cũ bị xóa mềm, thay bằng danh sách mới.
 *
 * @param title       tên bài tập, tối đa 255 ký tự
 * @param description mô tả
 * @param questions   danh sách câu hỏi mới, ít nhất một câu
 */
public record ExerciseUpdateRequest(
        @NotBlank(message = "Tên bài tập không được để trống") @Size(max = 255, message = "Tên bài tập tối đa 255 ký tự") String title,
        @NotBlank(message = "Mô tả không được để trống") String description,
        @NotEmpty(message = "Bài tập cần ít nhất một câu hỏi") List<@Valid ExerciseQuestionRequest> questions
) {
}
