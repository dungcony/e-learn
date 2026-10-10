package com.restaurant.modules.order.entity;

import com.restaurant.common.abstracts.AssignedIdEntity;
import com.restaurant.modules.order.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Đơn hàng của một bàn (bảng {@code orders}). Tên entity là {@code RestaurantOrder} vì {@code Order} là từ khóa của HQL.
 * Bàn, đặt bàn, khách hàng, nhân viên là id thuần của module khác.
 */
@Entity(name = "RestaurantOrder")
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(name = "table_id", nullable = false)
    private UUID tableId;

    @Column(name = "reservation_id")
    private UUID reservationId;

    // Sao chép từ đặt bàn lúc nhận bàn để hóa đơn gắn được với tài khoản khách hàng
    @Column(name = "customer_id")
    private UUID customerId;

    // Người mở đơn hoặc nhận bàn, cũng là "nhân viên phục vụ phụ trách bàn"
    @Column(name = "waiter_id", nullable = false)
    private UUID waiterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
