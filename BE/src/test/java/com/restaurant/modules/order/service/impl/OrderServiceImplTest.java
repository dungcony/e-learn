package com.restaurant.modules.order.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.modules.menu.dto.response.DishOrderingView;
import com.restaurant.modules.menu.enums.DishStatus;
import com.restaurant.modules.menu.service.DishService;
import com.restaurant.modules.order.dto.request.OpenOrderCommand;
import com.restaurant.modules.order.dto.request.OrderItemLineRequest;
import com.restaurant.modules.order.dto.request.OrderItemsAddRequest;
import com.restaurant.modules.order.dto.request.OrderOpenRequest;
import com.restaurant.modules.order.dto.request.OrderSearchRequest;
import com.restaurant.modules.order.dto.response.OrderPaymentView;
import com.restaurant.modules.order.dto.response.OrderResponse;
import com.restaurant.modules.order.dto.response.OrderSummaryResponse;
import com.restaurant.modules.order.entity.Order;
import com.restaurant.modules.order.entity.OrderItem;
import com.restaurant.modules.order.enums.OrderItemStatus;
import com.restaurant.modules.order.enums.OrderStatus;
import com.restaurant.modules.order.mapper.OrderMapperImpl;
import com.restaurant.modules.order.repository.OrderItemRepository;
import com.restaurant.modules.order.repository.OrderRepository;
import com.restaurant.modules.order.validator.OrderValidator;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.enums.TableZone;
import com.restaurant.modules.table.service.TableService;
import com.restaurant.modules.user.dto.response.UserBriefResponse;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-09T05:00:00Z");

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private TableService tableService;
    @Mock
    private DishService dishService;
    @Mock
    private UserService userService;

    private OrderServiceImpl service;
    private final UUID tableId = UUID.randomUUID();
    private final UUID waiterId = UUID.randomUUID();
    private TableBriefResponse tableBrief;
    private UserBriefResponse waiterBrief;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(orderRepository, orderItemRepository, new OrderValidator(), new OrderMapperImpl(),
                tableService, dishService, userService, Clock.fixed(NOW, ZoneOffset.UTC));
        tableBrief = new TableBriefResponse(tableId, "B05", TableZone.INDOOR, 6, TableStatus.OCCUPIED);
        waiterBrief = new UserBriefResponse(waiterId, "Lê Văn Phúc", "phuc@nhahang.vn", null, Role.WAITER);
    }

    private Order order(OrderStatus status) {
        Order o = new Order();
        o.setId(UUID.randomUUID());
        o.setTableId(tableId);
        o.setWaiterId(waiterId);
        o.setStatus(status);
        o.setOpenedAt(NOW);
        return o;
    }

    private OrderItem item(Order order, String dish, long price, int qty, OrderItemStatus status) {
        OrderItem i = new OrderItem();
        i.setId(UUID.randomUUID());
        i.setOrderId(order.getId());
        i.setDishId(UUID.randomUUID());
        i.setDishName(dish);
        i.setUnitPrice(price);
        i.setQuantity(qty);
        i.setStatus(status);
        i.setSentAt(NOW);
        return i;
    }

    private void stubDisplayLookups() {
        when(tableService.getTableBriefs(Set.of(tableId))).thenReturn(List.of(tableBrief));
        when(userService.getUserBriefs(Set.of(waiterId))).thenReturn(List.of(waiterBrief));
    }

    // ---- mở đơn ----

    @Test
    void openOrder_moves_table_to_occupied_and_creates_open_order_for_a_reservation() {
        UUID reservationId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(tableService.getTableBriefs(List.of(tableId))).thenReturn(List.of(tableBrief));
        when(tableService.transitionStatus(tableId, Set.of(TableStatus.AVAILABLE, TableStatus.RESERVED), TableStatus.OCCUPIED))
                .thenReturn(true);
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID orderId = service.openOrder(new OpenOrderCommand(tableId, reservationId, customerId, waiterId, true));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).saveAndFlush(captor.capture());
        Order saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(orderId);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.OPEN);
        assertThat(saved.getTableId()).isEqualTo(tableId);
        assertThat(saved.getReservationId()).isEqualTo(reservationId);
        assertThat(saved.getCustomerId()).isEqualTo(customerId);
        assertThat(saved.getWaiterId()).isEqualTo(waiterId);
        assertThat(saved.getOpenedAt()).isEqualTo(NOW);
    }

    @Test
    void openOrder_for_a_walk_in_only_accepts_an_available_table() {
        when(tableService.getTableBriefs(List.of(tableId))).thenReturn(List.of(tableBrief));
        when(tableService.transitionStatus(tableId, Set.of(TableStatus.AVAILABLE), TableStatus.OCCUPIED)).thenReturn(false);

        assertThatThrownBy(() -> service.openOrder(new OpenOrderCommand(tableId, null, null, waiterId, false)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_TABLE_NOT_OPENABLE");
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void openOrder_unknown_table_is_not_found() {
        when(tableService.getTableBriefs(List.of(tableId))).thenReturn(List.of());

        assertThatThrownBy(() -> service.openOrder(new OpenOrderCommand(tableId, null, null, waiterId, false)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
        verify(tableService, never()).transitionStatus(any(), any(), any());
    }

    @Test
    void openOrder_maps_open_order_unique_index_violation_to_not_openable() {
        when(tableService.getTableBriefs(List.of(tableId))).thenReturn(List.of(tableBrief));
        when(tableService.transitionStatus(any(), any(), any())).thenReturn(true);
        when(orderRepository.saveAndFlush(any(Order.class))).thenThrow(new DataIntegrityViolationException("uq_orders_open_table"));

        assertThatThrownBy(() -> service.openOrder(new OpenOrderCommand(tableId, null, null, waiterId, false)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_TABLE_NOT_OPENABLE");
    }

    @Test
    void openOrderForWalkIn_returns_the_new_empty_order_with_table_and_waiter() {
        when(tableService.getTableBriefs(List.of(tableId))).thenReturn(List.of(tableBrief));
        when(tableService.transitionStatus(tableId, Set.of(TableStatus.AVAILABLE), TableStatus.OCCUPIED)).thenReturn(true);
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById(any(UUID.class))).thenAnswer(inv -> {
            Order o = order(OrderStatus.OPEN);
            o.setId(inv.getArgument(0));
            return Optional.of(o);
        });
        stubDisplayLookups();
        when(orderItemRepository.findByOrderIdOrderBySentAtAscIdAsc(any(UUID.class))).thenReturn(List.of());

        OrderResponse response = service.openOrderForWalkIn(waiterId, new OrderOpenRequest(tableId));

        assertThat(response.status()).isEqualTo(OrderStatus.OPEN);
        assertThat(response.table().name()).isEqualTo("B05");
        assertThat(response.waiter().fullName()).isEqualTo("Lê Văn Phúc");
        assertThat(response.items()).isEmpty();
        assertThat(response.subtotal()).isZero();
    }

    // ---- xem đơn ----

    @Test
    void getOrder_computes_line_totals_subtotal_and_unserved_count_ignoring_cancelled() {
        Order order = order(OrderStatus.OPEN);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderBySentAtAscIdAsc(order.getId())).thenReturn(List.of(
                item(order, "Phở bò", 65000, 2, OrderItemStatus.SERVED),
                item(order, "Trà đá", 5000, 3, OrderItemStatus.READY),
                item(order, "Món hủy", 99000, 1, OrderItemStatus.CANCELLED)));
        stubDisplayLookups();

        OrderResponse response = service.getOrder(order.getId());

        assertThat(response.items()).extracting(i -> i.lineTotal()).containsExactly(130000L, 15000L, 99000L);
        assertThat(response.subtotal()).isEqualTo(145000L);
        assertThat(response.unservedCount()).isEqualTo(1);
    }

    @Test
    void getOrder_unknown_id_is_not_found() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(id))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchOrders_returns_summaries_with_item_count_and_subtotal() {
        Order order = order(OrderStatus.OPEN);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(order)));
        when(orderItemRepository.findByOrderIdIn(List.of(order.getId()))).thenReturn(List.of(
                item(order, "Phở bò", 65000, 2, OrderItemStatus.PENDING),
                item(order, "Món hủy", 99000, 1, OrderItemStatus.CANCELLED)));
        stubDisplayLookups();

        var page = service.searchOrders(new OrderSearchRequest(null, null), PageRequestParams.of(null, null, null, null));

        OrderSummaryResponse summary = page.getContent().get(0);
        assertThat(summary.itemCount()).isEqualTo(2);
        assertThat(summary.subtotal()).isEqualTo(130000L);
        assertThat(summary.waiterName()).isEqualTo("Lê Văn Phúc");
        assertThat(summary.table().name()).isEqualTo("B05");
    }

    // ---- gọi món và gửi bếp ----

    @Test
    void addItems_saves_pending_lines_with_name_and_price_snapshot() {
        Order order = order(OrderStatus.OPEN);
        UUID pho = UUID.randomUUID();
        UUID tra = UUID.randomUUID();
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(dishService.getDishesForOrdering(List.of(pho, tra))).thenReturn(List.of(
                new DishOrderingView(pho, "Phở bò tái", 65000, DishStatus.AVAILABLE),
                new DishOrderingView(tra, "Trà đá", 5000, DishStatus.AVAILABLE)));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        stubDisplayLookups();

        service.addItems(order.getId(), new OrderItemsAddRequest(List.of(
                new OrderItemLineRequest(pho, 2, "Không hành"), new OrderItemLineRequest(tra, 1, null))));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(captor.capture());
        List<OrderItem> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getDishName()).isEqualTo("Phở bò tái");
        assertThat(saved.get(0).getUnitPrice()).isEqualTo(65000L);
        assertThat(saved.get(0).getQuantity()).isEqualTo(2);
        assertThat(saved.get(0).getNote()).isEqualTo("Không hành");
        assertThat(saved).allSatisfy(i -> {
            assertThat(i.getOrderId()).isEqualTo(order.getId());
            assertThat(i.getStatus()).isEqualTo(OrderItemStatus.PENDING);
            assertThat(i.getSentAt()).isEqualTo(NOW);
            assertThat(i.getId()).isNotNull();
        });
    }

    @Test
    void addItems_same_dish_twice_with_different_notes_makes_two_lines() {
        Order order = order(OrderStatus.OPEN);
        UUID pho = UUID.randomUUID();
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(dishService.getDishesForOrdering(List.of(pho))).thenReturn(List.of(
                new DishOrderingView(pho, "Phở bò tái", 65000, DishStatus.AVAILABLE)));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        stubDisplayLookups();

        service.addItems(order.getId(), new OrderItemsAddRequest(List.of(
                new OrderItemLineRequest(pho, 1, "Không hành"), new OrderItemLineRequest(pho, 1, "Ít cay"))));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(OrderItem::getNote).containsExactly("Không hành", "Ít cay");
    }

    @Test
    void addItems_saves_nothing_when_any_dish_is_not_orderable() {
        Order order = order(OrderStatus.OPEN);
        UUID pho = UUID.randomUUID();
        UUID che = UUID.randomUUID();
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(dishService.getDishesForOrdering(List.of(pho, che))).thenReturn(List.of(
                new DishOrderingView(pho, "Phở bò tái", 65000, DishStatus.AVAILABLE),
                new DishOrderingView(che, "Chè", 30000, DishStatus.OUT_OF_STOCK)));

        assertThatThrownBy(() -> service.addItems(order.getId(), new OrderItemsAddRequest(List.of(
                new OrderItemLineRequest(pho, 1, null), new OrderItemLineRequest(che, 1, null)))))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("DISH_NOT_ORDERABLE");
        verify(orderItemRepository, never()).saveAll(any());
    }

    @Test
    void addItems_rejects_a_closed_order_before_looking_at_dishes() {
        Order order = order(OrderStatus.CLOSED);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.addItems(order.getId(), new OrderItemsAddRequest(
                List.of(new OrderItemLineRequest(UUID.randomUUID(), 1, null)))))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_NOT_OPEN");
        verify(dishService, never()).getDishesForOrdering(any());
    }

    @Test
    void addItems_unknown_order_is_not_found() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findByIdForUpdate(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addItems(id, new OrderItemsAddRequest(
                List.of(new OrderItemLineRequest(UUID.randomUUID(), 1, null)))))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    // ---- hủy đơn mở nhầm ----

    @Test
    void cancelEmptyOrder_deletes_the_order_and_frees_the_table() {
        Order order = order(OrderStatus.OPEN);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.existsByOrderId(order.getId())).thenReturn(false);

        service.cancelEmptyOrder(order.getId());

        verify(orderRepository).delete(order);
        verify(tableService).transitionStatus(tableId, Set.of(TableStatus.OCCUPIED), TableStatus.AVAILABLE);
    }

    @Test
    void cancelEmptyOrder_rejects_an_order_that_already_has_items() {
        Order order = order(OrderStatus.OPEN);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.existsByOrderId(order.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.cancelEmptyOrder(order.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_HAS_ITEMS");
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void cancelEmptyOrder_rejects_a_closed_order() {
        Order order = order(OrderStatus.CLOSED);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.existsByOrderId(order.getId())).thenReturn(false);

        assertThatThrownBy(() -> service.cancelEmptyOrder(order.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_NOT_OPEN");
    }

    // ---- API cho billing ----

    @Test
    void lockOrderForPayment_returns_lines_and_unserved_count() {
        Order order = order(OrderStatus.OPEN);
        order.setCustomerId(UUID.randomUUID());
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderBySentAtAscIdAsc(order.getId())).thenReturn(List.of(
                item(order, "Phở bò", 65000, 2, OrderItemStatus.SERVED),
                item(order, "Trà đá", 5000, 3, OrderItemStatus.COOKING),
                item(order, "Món hủy", 99000, 1, OrderItemStatus.CANCELLED)));

        OrderPaymentView view = service.lockOrderForPayment(order.getId());

        assertThat(view.orderId()).isEqualTo(order.getId());
        assertThat(view.tableId()).isEqualTo(tableId);
        assertThat(view.customerId()).isEqualTo(order.getCustomerId());
        assertThat(view.lines()).hasSize(3);
        assertThat(view.unservedCount()).isEqualTo(1);
    }

    @Test
    void lockOrderForPayment_rejects_a_closed_order_and_an_unknown_order() {
        Order closed = order(OrderStatus.CLOSED);
        when(orderRepository.findByIdForUpdate(closed.getId())).thenReturn(Optional.of(closed));
        UUID missing = UUID.randomUUID();
        when(orderRepository.findByIdForUpdate(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.lockOrderForPayment(closed.getId()))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo("ORDER_NOT_OPEN");
        assertThatThrownBy(() -> service.lockOrderForPayment(missing))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo("NOT_FOUND");
    }

    @Test
    void closeOrder_marks_the_order_closed_with_the_closing_time() {
        Order order = order(OrderStatus.OPEN);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

        service.closeOrder(order.getId());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CLOSED);
        assertThat(order.getClosedAt()).isEqualTo(NOW);
    }
}
