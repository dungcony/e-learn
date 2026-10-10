package com.restaurant.modules.order.repository;

import com.restaurant.modules.order.enums.OrderItemStatus;

import java.time.Instant;
import java.util.UUID;

/** Một dòng món trong hàng đợi bếp hoặc danh sách món đã xong, kèm bàn của đơn chứa nó. */
public interface QueueRow {

    UUID getId();

    UUID getOrderId();

    UUID getTableId();

    String getDishName();

    int getQuantity();

    String getNote();

    OrderItemStatus getStatus();

    Instant getSentAt();

    Instant getStartedAt();

    Instant getReadyAt();
}
