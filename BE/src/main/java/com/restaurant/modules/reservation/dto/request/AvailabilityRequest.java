package com.restaurant.modules.reservation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.BindParam;

import java.time.Instant;

/**
 * Kiểm tra còn bàn trước khi đặt, nhận từ query string. Giờ có dấu cộng của múi giờ (vd {@code +07:00}) phải mã hóa thành
 * {@code %2B} trong URL.
 *
 * @param reservedAt giờ hẹn, ISO-8601 có múi giờ
 * @param guestCount số khách từ 1 đến 50
 */
public record AvailabilityRequest(
        @BindParam("reserved_at") @NotNull(message = "Giờ hẹn không được để trống") Instant reservedAt,
        @BindParam("guest_count") @NotNull(message = "Số khách không được để trống")
        @Min(value = 1, message = "Số khách tối thiểu là 1") @Max(value = 50, message = "Số khách tối đa là 50") Integer guestCount) {
}
