package com.restaurant.modules.billing.repository;

import java.util.UUID;

/** Một dòng kết quả món bán chạy: tổng số lượng bán và doanh thu (chưa VAT) của món trong khoảng báo cáo. */
public interface TopDishRow {

    UUID getDishId();

    String getDishName();

    long getQuantity();

    long getRevenue();
}
