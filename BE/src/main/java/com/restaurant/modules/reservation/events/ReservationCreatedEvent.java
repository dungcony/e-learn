package com.restaurant.modules.reservation.events;

import java.time.Instant;

/** Đặt bàn mới được tạo; gửi email "đã nhận yêu cầu" nếu khách có email. */
public record ReservationCreatedEvent(String email, String code, String guestName, Instant reservedAt, int guestCount) {
}
