package com.elearning.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sửa thể loại khóa học (UC015).
 *
 * @param name tên thể loại mới, tối đa 255 ký tự, không trùng thể loại khác (không phân biệt hoa thường)
 */
public record CategoryUpdateRequest(
        @NotBlank(message = "Tên thể loại không được để trống") @Size(max = 255, message = "Tên thể loại tối đa 255 ký tự") String name
) {
}
