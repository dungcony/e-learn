package com.restaurant.modules.reservation.events;

import java.time.Instant;

/** Đặt bàn được xác nhận và gán bàn; gửi email kết quả nếu khách có email. */
public record ReservationConfirmedEvent(String email, String code, String guestName, Instant reservedAt, String tableName) {
}
