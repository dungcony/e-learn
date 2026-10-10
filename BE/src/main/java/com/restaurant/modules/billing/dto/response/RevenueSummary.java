package com.restaurant.modules.billing.dto.response;

/** Tổng cộng của toàn khoảng báo cáo; toàn 0 khi không có hóa đơn nào. */
public record RevenueSummary(long invoiceCount, long subtotal, long vatAmount, long totalAmount) {
}
