package com.restaurant.modules.menu.dto.response;

import com.restaurant.modules.menu.enums.DishCategory;
import com.restaurant.modules.menu.enums.DishStatus;

import java.time.Instant;
import java.util.UUID;

public record DishDetailResponse(
        UUID id,
        String name,
        DishCategory category,
        long price,
        String unit,
        String description,
        Integer prepTimeMinutes,
        String imageUrl,
        DishStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
