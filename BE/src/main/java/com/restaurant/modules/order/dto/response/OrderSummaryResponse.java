package com.restaurant.modules.order.dto.response;

import com.restaurant.modules.order.enums.OrderStatus;
import com.restaurant.modules.table.dto.response.TableBriefResponse;

import java.time.Instant;
import java.util.UUID;

/** Một dòng trong danh sách đơn; {@code subtotal} là tổng thành tiền các dòng không bị hủy. */
public record OrderSummaryResponse(
        UUID id,
        TableBriefResponse table,
        OrderStatus status,
        String waiterName,
        Instant openedAt,
        int itemCount,
        long subtotal) {
}
