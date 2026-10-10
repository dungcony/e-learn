package com.restaurant.modules.reservation.dto.response;

import com.restaurant.modules.reservation.enums.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

/** Một dòng trong danh sách đặt bàn. */
public record ReservationSummaryResponse(
        UUID id,
        String code,
        String guestName,
        String phone,
        Instant reservedAt,
        int guestCount,
        ReservationStatus status,
        String tableName) {
}
