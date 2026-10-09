package com.elearning.info.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sửa tin tức (UC012). Gửi đủ tiêu đề và nội dung.
 *
 * @param title   tiêu đề, tối đa 255 ký tự
 * @param content nội dung
 */
public record NewsUpdateRequest(
        @NotBlank(message = "Tiêu đề không được để trống") @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự") String title,
        @NotBlank(message = "Nội dung không được để trống") String content
) {
}
