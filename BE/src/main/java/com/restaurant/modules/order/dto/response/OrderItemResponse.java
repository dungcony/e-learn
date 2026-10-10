package com.restaurant.modules.order.dto.response;

import com.restaurant.modules.order.enums.OrderItemStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * @param unitPrice giá chụp lúc gửi bếp, VND
 * @param lineTotal {@code unitPrice × quantity}, tính khi đọc
 */
public record OrderItemResponse(
        UUID id,
        UUID dishId,
        String dishName,
        long unitPrice,
        int quantity,
        long lineTotal,
        String note,
        OrderItemStatus status,
        Instant sentAt,
        Instant startedAt,
        Instant readyAt,
        Instant servedAt) {
}
