package com.restaurant.modules.menu.dto.response;

import com.restaurant.modules.menu.enums.DishCategory;

/**
 * @param dishCount số món đang hiển thị trong thực đơn ({@code AVAILABLE} và {@code OUT_OF_STOCK}); client tự ánh xạ tên
 *                  tiếng Việt từ {@code category}
 */
public record MenuCategoryResponse(DishCategory category, long dishCount) {
}
