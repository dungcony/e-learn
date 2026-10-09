package com.elearning.info.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Đăng tin tức (UC012, Bảng 2-27).
 *
 * @param title   tiêu đề, tối đa 255 ký tự
 * @param content nội dung
 */
public record NewsCreateRequest(
        @NotBlank(message = "Tiêu đề không được để trống") @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự") String title,
        @NotBlank(message = "Nội dung không được để trống") String content
) {
}
