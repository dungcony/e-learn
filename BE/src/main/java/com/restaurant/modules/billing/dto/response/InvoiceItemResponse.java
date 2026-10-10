package com.restaurant.modules.billing.dto.response;

/** Một dòng món trên hóa đơn; {@code lineTotal = unitPrice × quantity}, VND. */
public record InvoiceItemResponse(String dishName, long unitPrice, int quantity, long lineTotal) {
}
