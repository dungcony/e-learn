package com.restaurant.modules.order.repository;

import com.restaurant.modules.order.entity.OrderItem;
import com.restaurant.modules.order.enums.OrderItemStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderIdOrderBySentAtAscIdAsc(UUID orderId);

    List<OrderItem> findByOrderIdIn(Collection<UUID> orderIds);

    boolean existsByOrderId(UUID orderId);

    boolean existsByDishId(UUID dishId);

    boolean existsByChefId(UUID chefId);

    /** Hàng đợi bếp: dòng món của các đơn đang mở theo trạng thái cho trước, gửi sớm nhất trước. */
    @Query("""
            select i.id as id, i.orderId as orderId, o.tableId as tableId, i.dishName as dishName, i.quantity as quantity,
                   i.note as note, i.status as status, i.sentAt as sentAt, i.startedAt as startedAt, i.readyAt as readyAt
            from OrderItem i, RestaurantOrder o
            where i.orderId = o.id and o.status = com.restaurant.modules.order.enums.OrderStatus.OPEN and i.status in :statuses
            order by i.sentAt asc, i.id asc
            """)
    List<QueueRow> findQueue(@Param("statuses") Collection<OrderItemStatus> statuses, Pageable pageable);

    /** Món đã xong của các đơn đang mở do nhân viên này phụ trách, xong sớm nhất trước. */
    @Query("""
            select i.id as id, i.orderId as orderId, o.tableId as tableId, i.dishName as dishName, i.quantity as quantity,
                   i.note as note, i.status as status, i.sentAt as sentAt, i.startedAt as startedAt, i.readyAt as readyAt
            from OrderItem i, RestaurantOrder o
            where i.orderId = o.id and o.status = com.restaurant.modules.order.enums.OrderStatus.OPEN
              and i.status = com.restaurant.modules.order.enums.OrderItemStatus.READY and o.waiterId = :waiterId
            order by i.readyAt asc, i.id asc
            """)
    List<QueueRow> findReadyForWaiter(@Param("waiterId") UUID waiterId, Pageable pageable);

    // Ba câu UPDATE dưới đây chỉ đổi khi dòng đang ở đúng trạng thái trước đó và đơn còn mở, nên hai bếp cùng bấm "nhận" thì chỉ một
    // người thành công. Trả số dòng đã đổi; 0 nghĩa là thao tác trùng, sai trạng thái hoặc đơn đã đóng.

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update OrderItem i set i.status = com.restaurant.modules.order.enums.OrderItemStatus.COOKING,
                   i.startedAt = :now, i.chefId = :chefId, i.updatedAt = :now
            where i.id = :id and i.status = com.restaurant.modules.order.enums.OrderItemStatus.PENDING
              and exists (select 1 from RestaurantOrder o where o.id = i.orderId
                          and o.status = com.restaurant.modules.order.enums.OrderStatus.OPEN)
            """)
    int markCooking(@Param("id") UUID id, @Param("chefId") UUID chefId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update OrderItem i set i.status = com.restaurant.modules.order.enums.OrderItemStatus.READY,
                   i.readyAt = :now, i.updatedAt = :now
            where i.id = :id and i.status = com.restaurant.modules.order.enums.OrderItemStatus.COOKING
              and exists (select 1 from RestaurantOrder o where o.id = i.orderId
                          and o.status = com.restaurant.modules.order.enums.OrderStatus.OPEN)
            """)
    int markReady(@Param("id") UUID id, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update OrderItem i set i.status = com.restaurant.modules.order.enums.OrderItemStatus.SERVED,
                   i.servedAt = :now, i.updatedAt = :now
            where i.id = :id and i.status = com.restaurant.modules.order.enums.OrderItemStatus.READY
              and exists (select 1 from RestaurantOrder o where o.id = i.orderId
                          and o.status = com.restaurant.modules.order.enums.OrderStatus.OPEN)
            """)
    int markServed(@Param("id") UUID id, @Param("now") Instant now);
}
