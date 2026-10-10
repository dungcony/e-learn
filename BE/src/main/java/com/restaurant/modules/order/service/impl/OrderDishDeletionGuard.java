package com.restaurant.modules.order.service.impl;

import com.restaurant.modules.menu.service.DishDeletionGuard;
import com.restaurant.modules.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Món đã từng nằm trong một đơn hàng thì không xóa được, chỉ chuyển sang ngừng bán. */
@Component
@RequiredArgsConstructor
public class OrderDishDeletionGuard implements DishDeletionGuard {

    private final OrderItemRepository orderItemRepository;

    @Override
    public boolean isUsed(UUID dishId) {
        return orderItemRepository.existsByDishId(dishId);
    }
}
