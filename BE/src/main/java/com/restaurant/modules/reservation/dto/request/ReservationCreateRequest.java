package com.restaurant.modules.reservation.dto.request;

import com.restaurant.modules.table.enums.TableZone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Đặt bàn trực tuyến của Khách hoặc Khách hàng (UC013, Bảng 2-27).
 *
 * @param reservedAt    giờ hẹn, ISO-8601 có múi giờ; trong giờ mở cửa, trước ít nhất 2 giờ và tối đa 30 ngày
 * @param preferredZone khu vực ưu tiên, chỉ là gợi ý cho nhân viên khi gán bàn
 */
public record ReservationCreateRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
        String guestName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "\\d{10}", message = "Số điện thoại phải gồm 10 chữ số")
        String phone,

        @Email(message = "Email không đúng định dạng")
        @Size(max = 255, message = "Email tối đa 255 ký tự")
        String email,

        @NotNull(message = "Giờ hẹn không được để trống")
        Instant reservedAt,

        @NotNull(message = "Số khách không được để trống")
        @Min(value = 1, message = "Số khách tối thiểu là 1")
        @Max(value = 50, message = "Số khách tối đa là 50")
        Integer guestCount,

        TableZone preferredZone,

        @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
        String note) {
}
