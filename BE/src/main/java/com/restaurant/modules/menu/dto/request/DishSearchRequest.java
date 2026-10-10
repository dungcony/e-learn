package com.restaurant.modules.menu.dto.request;

import com.restaurant.modules.menu.enums.DishCategory;
import com.restaurant.modules.menu.enums.DishStatus;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.BindParam;

/**
 * Tiêu chí tìm món (UC007, Bảng 2-14), nhận từ query string; mọi tiêu chí tùy chọn, kết hợp bằng AND.
 *
 * @param name     tên món chứa chuỗi, không phân biệt hoa thường
 * @param minPrice giá từ (VND, gồm đầu mút)
 * @param maxPrice giá đến (VND, gồm đầu mút); {@code minPrice} không được lớn hơn
 * @param status   thực đơn công khai không nhận {@code DISCONTINUED}
 */
public record DishSearchRequest(
        String name,
        DishCategory category,
        @BindParam("min_price") @PositiveOrZero(message = "Giá không được âm") Long minPrice,
        @BindParam("max_price") @PositiveOrZero(message = "Giá không được âm") Long maxPrice,
        DishStatus status) {
}
