package com.restaurant.modules.billing.dto.response;

import com.restaurant.modules.table.dto.response.TableBriefResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Tạm tính của đơn trước khi thanh toán (dòng {@code CANCELLED} đã được loại). Số tiền là VND.
 *
 * @param vatRate       tỷ lệ VAT đang áp dụng, vd {@code 0.08}
 * @param unservedCount số dòng chưa phục vụ, để cảnh báo trước khi thanh toán
 */
public record PaymentSummaryResponse(
        UUID orderId,
        TableBriefResponse table,
        Instant openedAt,
        List<PaymentLineResponse> lines,
        long subtotal,
        BigDecimal vatRate,
        long vatAmount,
        long totalAmount,
        int unservedCount) {
}
