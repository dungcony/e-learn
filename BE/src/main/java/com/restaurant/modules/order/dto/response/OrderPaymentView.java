package com.restaurant.modules.order.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Dữ liệu của đơn đã khóa mà module thanh toán cần; không có endpoint riêng.
 *
 * @param unservedCount số dòng chưa phục vụ, để cảnh báo Thu ngân trước khi thanh toán
 */
public record OrderPaymentView(
        UUID orderId,
        UUID tableId,
        UUID reservationId,
        UUID customerId,
        UUID waiterId,
        Instant openedAt,
        List<PaymentLine> lines,
        int unservedCount) {
}
