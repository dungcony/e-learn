package com.restaurant.modules.billing.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Doanh thu của một ngày hoặc một tháng; báo cáo theo ngày chỉ có {@code date} ({@code yyyy-MM-dd}), báo cáo theo tháng chỉ có
 * {@code month} ({@code yyyy-MM}), trường còn lại không xuất hiện trong JSON. Số tiền là VND.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RevenuePoint(String date, String month, long invoiceCount, long subtotal, long vatAmount, long totalAmount) {
}
