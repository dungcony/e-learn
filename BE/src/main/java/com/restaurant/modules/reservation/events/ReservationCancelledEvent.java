package com.restaurant.modules.reservation.events;

import java.time.Instant;

/** Đặt bàn bị nhân viên từ chối hoặc hủy; gửi email kèm lý do nếu khách có email. */
public record ReservationCancelledEvent(String email, String code, String guestName, Instant reservedAt, String reason) {
}
