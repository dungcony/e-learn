package com.restaurant.modules.billing.dto.response;

import java.util.UUID;

/**
 * Một món trong báo cáo bán chạy.
 *
 * @param quantity tổng số lượng bán
 * @param revenue  tổng thành tiền, VND (chưa gồm VAT)
 */
public record TopDishItem(UUID dishId, String dishName, long quantity, long revenue) {
}
