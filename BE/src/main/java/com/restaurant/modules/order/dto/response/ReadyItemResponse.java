package com.restaurant.modules.order.dto.response;

import java.time.Instant;
import java.util.UUID;

/** Món Bếp đã xong, chờ nhân viên phục vụ mang ra bàn. */
public record ReadyItemResponse(UUID id, UUID orderId, String tableName, String dishName, int quantity, String note, Instant readyAt) {
}
