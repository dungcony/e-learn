package com.restaurant.modules.reservation.dto.request;

import java.util.UUID;

/** @param tableId bàn khác thay cho bàn đã gán khi bàn đó chưa dọn xong; bỏ trống thì nhận bàn đã gán */
public record ReservationCheckInRequest(UUID tableId) {
}
