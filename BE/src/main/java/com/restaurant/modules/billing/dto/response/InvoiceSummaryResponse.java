package com.restaurant.modules.billing.dto.response;

import com.restaurant.modules.billing.enums.PaymentMethod;

import java.time.Instant;
import java.util.UUID;

/** Dòng hóa đơn trong danh sách lịch sử; {@code totalAmount} là VND đã gồm VAT. */
public record InvoiceSummaryResponse(UUID id, String code, String tableName, Instant paidAt, long totalAmount, PaymentMethod paymentMethod) {
}
