package com.elearning.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Thêm thể loại khóa học (UC015, Bảng 2-32).
 *
 * @param name tên thể loại, tối đa 255 ký tự, không trùng thể loại khác (không phân biệt hoa thường)
 */
public record CategoryCreateRequest(
        @NotBlank(message = "Tên thể loại không được để trống") @Size(max = 255, message = "Tên thể loại tối đa 255 ký tự") String name
) {
}
