package com.restaurant.modules.billing.dto.response;

import java.util.List;

/** Món bán chạy nhất, xếp theo số lượng giảm dần rồi doanh thu giảm dần. */
public record TopDishesResponse(List<TopDishItem> items) {
}
