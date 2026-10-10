package com.restaurant.modules.order.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.modules.order.dto.response.KitchenItemResponse;
import com.restaurant.modules.order.dto.response.OrderItemResponse;
import com.restaurant.modules.order.dto.response.ReadyItemResponse;
import com.restaurant.modules.order.entity.Order;
import com.restaurant.modules.order.entity.OrderItem;
import com.restaurant.modules.order.enums.OrderItemStatus;
import com.restaurant.modules.order.enums.OrderStatus;
import com.restaurant.modules.order.mapper.OrderMapperImpl;
import com.restaurant.modules.order.repository.OrderItemRepository;
import com.restaurant.modules.order.repository.OrderRepository;
import com.restaurant.modules.order.repository.QueueRow;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.enums.TableZone;
import com.restaurant.modules.table.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-09T05:00:00Z");

    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private TableService tableService;

    private OrderItemServiceImpl service;
    private final UUID tableId = UUID.randomUUID();
    private final UUID waiterId = UUID.randomUUID();
    private final UUID chefId = UUID.randomUUID();
    private Order order;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        service = new OrderItemServiceImpl(orderItemRepository, orderRepository, tableService, new OrderMapperImpl(),
                Clock.fixed(NOW, ZoneOffset.UTC));
        order = new Order();
        order.setId(UUID.randomUUID());
        order.setTableId(tableId);
        order.setWaiterId(waiterId);
        order.setStatus(OrderStatus.OPEN);
        item = new OrderItem();
        item.setId(UUID.randomUUID());
        item.setOrderId(order.getId());
        item.setDishId(UUID.randomUUID());
        item.setDishName("Phở bò tái");
        item.setUnitPrice(65000);
        item.setQuantity(2);
        item.setStatus(OrderItemStatus.PENDING);
        item.setSentAt(NOW);
    }

    private QueueRow row(OrderItemStatus status, Instant started, Instant ready) {
        return new QueueRow() {
            public UUID getId() { return item.getId(); }
            public UUID getOrderId() { return order.getId(); }
            public UUID getTableId() { return tableId; }
            public String getDishName() { return "Phở bò tái"; }
            public int getQuantity() { return 2; }
            public String getNote() { return "Không hành"; }
            public OrderItemStatus getStatus() { return status; }
            public Instant getSentAt() { return NOW; }
            public Instant getStartedAt() { return started; }
            public Instant getReadyAt() { return ready; }
        };
    }

    private void stubTableName() {
        when(tableService.getTableBriefs(Set.of(tableId)))
                .thenReturn(List.of(new TableBriefResponse(tableId, "B05", TableZone.INDOOR, 6, TableStatus.OCCUPIED)));
    }

    // ---- hàng đợi bếp ----

    @Test
    void getKitchenQueue_defaults_to_pending_and_cooking_with_table_names() {
        when(orderItemRepository.findQueue(Set.of(OrderItemStatus.PENDING, OrderItemStatus.COOKING), PageRequest.of(0, 200)))
                .thenReturn(List.of(row(OrderItemStatus.PENDING, null, null)));
        stubTableName();

        List<KitchenItemResponse> queue = service.getKitchenQueue(null);

        assertThat(queue).singleElement().satisfies(q -> {
            assertThat(q.tableName()).isEqualTo("B05");
            assertThat(q.dishName()).isEqualTo("Phở bò tái");
            assertThat(q.quantity()).isEqualTo(2);
            assertThat(q.note()).isEqualTo("Không hành");
            assertThat(q.status()).isEqualTo(OrderItemStatus.PENDING);
        });
    }

    @Test
    void getKitchenQueue_filters_by_one_status() {
        when(orderItemRepository.findQueue(Set.of(OrderItemStatus.COOKING), PageRequest.of(0, 200))).thenReturn(List.of());

        assertThat(service.getKitchenQueue(OrderItemStatus.COOKING)).isEmpty();
    }

    @Test
    void getKitchenQueue_rejects_statuses_other_than_pending_and_cooking() {
        assertThatThrownBy(() -> service.getKitchenQueue(OrderItemStatus.SERVED))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void getReadyItems_returns_only_the_waiters_ready_items() {
        when(orderItemRepository.findReadyForWaiter(waiterId, PageRequest.of(0, 200)))
                .thenReturn(List.of(row(OrderItemStatus.READY, NOW, NOW)));
        stubTableName();

        List<ReadyItemResponse> ready = service.getReadyItems(waiterId);

        assertThat(ready).singleElement().satisfies(r -> {
            assertThat(r.tableName()).isEqualTo("B05");
            assertThat(r.readyAt()).isEqualTo(NOW);
        });
    }

    // ---- bếp nhận và hoàn thành, phục vụ xác nhận ----

    @Test
    void startCooking_returns_the_updated_item() {
        when(orderItemRepository.markCooking(item.getId(), chefId, NOW)).thenReturn(1);
        item.setStatus(OrderItemStatus.COOKING);
        item.setStartedAt(NOW);
        item.setChefId(chefId);
        when(orderItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        stubTableName();

        KitchenItemResponse response = service.startCooking(item.getId(), chefId);

        assertThat(response.status()).isEqualTo(OrderItemStatus.COOKING);
        assertThat(response.startedAt()).isEqualTo(NOW);
        assertThat(response.tableName()).isEqualTo("B05");
    }

    @Test
    void markReady_returns_the_updated_item() {
        when(orderItemRepository.markReady(item.getId(), NOW)).thenReturn(1);
        item.setStatus(OrderItemStatus.READY);
        item.setReadyAt(NOW);
        when(orderItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        stubTableName();

        assertThat(service.markReady(item.getId()).status()).isEqualTo(OrderItemStatus.READY);
    }

    @Test
    void markServed_returns_the_updated_item_with_line_total() {
        when(orderItemRepository.markServed(item.getId(), NOW)).thenReturn(1);
        item.setStatus(OrderItemStatus.SERVED);
        item.setServedAt(NOW);
        when(orderItemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        OrderItemResponse response = service.markServed(item.getId());

        assertThat(response.status()).isEqualTo(OrderItemStatus.SERVED);
        assertThat(response.lineTotal()).isEqualTo(130000L);
        assertThat(response.servedAt()).isEqualTo(NOW);
    }

    @Test
    void zero_rows_changed_and_unknown_item_is_not_found() {
        UUID id = UUID.randomUUID();
        when(orderItemRepository.markCooking(id, chefId, NOW)).thenReturn(0);
        when(orderItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startCooking(id, chefId))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo("NOT_FOUND");
    }

    @Test
    void zero_rows_changed_and_closed_order_is_order_not_open() {
        order.setStatus(OrderStatus.CLOSED);
        when(orderItemRepository.markReady(item.getId(), NOW)).thenReturn(0);
        when(orderItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.markReady(item.getId()))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo("ORDER_NOT_OPEN");
    }

    @Test
    void zero_rows_changed_on_an_open_order_means_someone_else_already_handled_the_item() {
        when(orderItemRepository.markCooking(item.getId(), chefId, NOW)).thenReturn(0);
        when(orderItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.startCooking(item.getId(), chefId))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo("ORDER_ITEM_STATUS_CONFLICT");
    }

    @Test
    void markServed_conflict_when_the_item_is_not_ready() {
        when(orderItemRepository.markServed(item.getId(), NOW)).thenReturn(0);
        when(orderItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.markServed(item.getId()))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo("ORDER_ITEM_STATUS_CONFLICT");
        verify(orderItemRepository, never()).markReady(item.getId(), NOW);
    }
}
