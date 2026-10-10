package com.restaurant.modules.reservation.dto.response;

import com.restaurant.modules.reservation.enums.ReservationStatus;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.enums.TableZone;

import java.time.Instant;
import java.util.UUID;

/** Chi tiết đặt bàn; {@code table} là bàn được gán (null khi chưa xác nhận). */
public record ReservationResponse(
        UUID id,
        String code,
        ReservationStatus status,
        String guestName,
        String phone,
        String email,
        Instant reservedAt,
        Instant reservedEnd,
        int guestCount,
        TableZone preferredZone,
        String note,
        TableBriefResponse table,
        String cancelReason,
        Instant createdAt) {
}
