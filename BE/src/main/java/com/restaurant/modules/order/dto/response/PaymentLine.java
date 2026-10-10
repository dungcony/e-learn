package com.restaurant.modules.order.dto.response;

import com.restaurant.modules.order.enums.OrderItemStatus;

import java.util.UUID;

/** Một dòng món của đơn để module thanh toán tính tiền; module đó tự loại dòng {@code CANCELLED}. */
public record PaymentLine(UUID dishId, String dishName, long unitPrice, int quantity, OrderItemStatus status) {
}
