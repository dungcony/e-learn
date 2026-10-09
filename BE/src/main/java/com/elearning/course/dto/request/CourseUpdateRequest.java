package com.elearning.course.dto.request;

import com.elearning.course.enums.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Sửa khóa học (UC009). Gửi đủ form; mã khóa học không đổi. Đổi {@code status} là Mở khóa/Khóa khóa học.
 *
 * @param title              tên khóa học, tối đa 255 ký tự
 * @param description        mô tả
 * @param startDate          ngày bắt đầu, {@code yyyy-MM-dd}
 * @param endDate            ngày kết thúc, phải sau {@code startDate}
 * @param status             {@code PUBLIC} hoặc {@code PRIVATE}
 * @param categoryId         id thể loại
 * @param price              giá (VND), không âm; bỏ trống là 0
 * @param referenceMaterials thông tin hoặc đường dẫn tài liệu tham khảo
 */
public record CourseUpdateRequest(
        @NotBlank(message = "Tên khóa học không được để trống") @Size(max = 255, message = "Tên khóa học tối đa 255 ký tự") String title,
        @NotBlank(message = "Mô tả không được để trống") String description,
        @NotNull(message = "Thời gian bắt đầu không được để trống") LocalDate startDate,
        @NotNull(message = "Thời gian kết thúc không được để trống") LocalDate endDate,
        @NotNull(message = "Trạng thái không được để trống") CourseStatus status,
        @NotNull(message = "Thể loại không được để trống") UUID categoryId,
        @PositiveOrZero(message = "Giá không được âm") BigDecimal price,
        String referenceMaterials
) {
}
