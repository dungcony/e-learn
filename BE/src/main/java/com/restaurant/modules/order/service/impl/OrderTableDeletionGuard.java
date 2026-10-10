package com.restaurant.modules.order.service.impl;

import com.restaurant.modules.order.enums.OrderStatus;
import com.restaurant.modules.order.repository.OrderRepository;
import com.restaurant.modules.table.service.TableDeletionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Bàn còn đơn đang mở thì không xóa hay tạm khóa được. Đơn hàng không có số khách nên không giới hạn sức chứa. */
@Component
@RequiredArgsConstructor
public class OrderTableDeletionGuard implements TableDeletionGuard {

    private final OrderRepository orderRepository;

    @Override
    public boolean hasBlockingData(UUID tableId) {
        return orderRepository.existsByTableIdAndStatus(tableId, OrderStatus.OPEN);
    }

    @Override
    public int maxGuestCountAssigned(UUID tableId) {
        return 0;
    }
}
