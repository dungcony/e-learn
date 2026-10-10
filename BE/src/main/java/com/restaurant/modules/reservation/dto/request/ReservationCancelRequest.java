package com.restaurant.modules.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** @param reason lý do từ chối hoặc hủy, gửi kèm email cho khách */
public record ReservationCancelRequest(
        @NotBlank(message = "Lý do không được để trống")
        @Size(max = 255, message = "Lý do tối đa 255 ký tự")
        String reason) {
}
