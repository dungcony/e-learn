package com.restaurant.modules.order.dto.response;

import com.restaurant.modules.order.enums.OrderStatus;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.user.dto.response.UserBriefResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Chi tiết đơn hàng của một bàn.
 *
 * @param subtotal      tổng thành tiền các dòng không bị hủy, VND (chưa gồm VAT)
 * @param unservedCount số dòng chưa phục vụ ({@code PENDING}, {@code COOKING}, {@code READY})
 */
public record OrderResponse(
        UUID id,
        TableBriefResponse table,
        OrderStatus status,
        UUID reservationId,
        UserBriefResponse waiter,
        Instant openedAt,
        Instant closedAt,
        List<OrderItemResponse> items,
        long subtotal,
        int unservedCount) {
}
