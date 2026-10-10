package com.restaurant.modules.billing.repository;

/** Một dòng kết quả gộp doanh thu; {@code period} là {@code yyyy-MM-dd} (theo ngày) hoặc {@code yyyy-MM} (theo tháng). */
public interface RevenueRow {

    String getPeriod();

    long getInvoiceCount();

    long getSubtotal();

    long getVatAmount();

    long getTotalAmount();
}
