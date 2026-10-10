package com.restaurant.modules.order.service.impl;

import com.restaurant.modules.order.repository.OrderItemRepository;
import com.restaurant.modules.order.repository.OrderRepository;
import com.restaurant.modules.user.service.UserDeletionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Nhân viên đã mở đơn (phục vụ) hoặc đã nhận chế biến món (bếp) thì chỉ khóa được, không xóa. */
@Component
@RequiredArgsConstructor
public class OrderUserDeletionGuard implements UserDeletionGuard {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public boolean hasBlockingData(UUID userId) {
        return orderRepository.existsByWaiterId(userId) || orderItemRepository.existsByChefId(userId);
    }
}
