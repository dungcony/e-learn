package com.restaurant.modules.order.repository;

import com.restaurant.modules.order.entity.Order;
import com.restaurant.modules.order.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    /**
     * Khóa dòng đơn ({@code SELECT ... FOR UPDATE}) để thêm món, hủy đơn rỗng hay thanh toán không chạy chồng nhau. Mọi nơi
     * cần khóa nhiều bản ghi phải theo thứ tự đặt bàn → đơn → bàn.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from RestaurantOrder o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByWaiterId(UUID waiterId);

    boolean existsByTableIdAndStatus(UUID tableId, OrderStatus status);
}
