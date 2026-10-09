package com.elearning.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * Sửa bài giảng (UC011). Gửi đủ form.
 *
 * @param title       tên bài giảng, tối đa 255 ký tự
 * @param description mô tả, tùy chọn
 * @param contentUrl  đường dẫn tới video hoặc tài liệu, phải là URL
 */
public record LectureUpdateRequest(
        @NotBlank(message = "Tên bài giảng không được để trống") @Size(max = 255, message = "Tên bài giảng tối đa 255 ký tự") String title,
        String description,
        @NotBlank(message = "Đường dẫn tài liệu không được để trống") @Size(max = 1000, message = "Đường dẫn tối đa 1000 ký tự")
        @URL(message = "Đường dẫn tài liệu phải là URL") String contentUrl
) {
}
