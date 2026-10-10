package com.restaurant.modules.reservation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** @param tableId bàn gán cho đặt bàn; phải đủ sức chứa, không tạm khóa và còn trống trong khung giờ hẹn */
public record ReservationConfirmRequest(@NotNull(message = "Bàn không được để trống") UUID tableId) {
}
