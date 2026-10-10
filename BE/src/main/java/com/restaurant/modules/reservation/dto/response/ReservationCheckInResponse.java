package com.restaurant.modules.reservation.dto.response;

import java.util.UUID;

/** @param orderId đơn hàng vừa mở cho bàn khi nhận bàn, để nhân viên gọi món ngay */
public record ReservationCheckInResponse(ReservationResponse reservation, UUID orderId) {
}
