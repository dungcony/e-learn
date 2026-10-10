package com.restaurant.modules.menu.dto.response;

import com.restaurant.modules.menu.enums.DishStatus;

import java.util.UUID;

/** Dữ liệu tối thiểu module gọi món cần để chụp tên và giá lúc gửi bếp; không có endpoint riêng. */
public record DishOrderingView(UUID id, String name, long price, DishStatus status) {
}
