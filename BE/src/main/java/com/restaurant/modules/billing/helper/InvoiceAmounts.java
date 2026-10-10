package com.restaurant.modules.billing.helper;

/** Kết quả tính tiền của một đơn, VND: {@code totalAmount = subtotal + vatAmount}. */
public record InvoiceAmounts(long subtotal, long vatAmount, long totalAmount) {
}
