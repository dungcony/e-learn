package com.restaurant.modules.order.dto.response;

import com.restaurant.modules.order.enums.OrderItemStatus;

import java.time.Instant;
import java.util.UUID;

/** Một dòng món trong hàng đợi của Bếp. */
public record KitchenItemResponse(
        UUID id,
        UUID orderId,
        String tableName,
        String dishName,
        int quantity,
        String note,
        OrderItemStatus status,
        Instant sentAt,
        Instant startedAt) {
}
