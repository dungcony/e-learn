package com.restaurant.modules.order.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.modules.menu.dto.response.DishOrderingView;
import com.restaurant.modules.menu.service.DishService;
import com.restaurant.modules.order.dto.request.OpenOrderCommand;
import com.restaurant.modules.order.dto.request.OrderItemLineRequest;
import com.restaurant.modules.order.dto.request.OrderItemsAddRequest;
import com.restaurant.modules.order.dto.request.OrderOpenRequest;
import com.restaurant.modules.order.dto.request.OrderSearchRequest;
import com.restaurant.modules.order.dto.response.OrderItemResponse;
import com.restaurant.modules.order.dto.response.OrderPaymentView;
import com.restaurant.modules.order.dto.response.OrderResponse;
import com.restaurant.modules.order.dto.response.OrderSummaryResponse;
import com.restaurant.modules.order.dto.response.PaymentLine;
import com.restaurant.modules.order.entity.Order;
import com.restaurant.modules.order.entity.OrderItem;
import com.restaurant.modules.order.enums.OrderItemStatus;
import com.restaurant.modules.order.enums.OrderStatus;
import com.restaurant.modules.order.mapper.OrderMapper;
import com.restaurant.modules.order.repository.OrderItemRepository;
import com.restaurant.modules.order.repository.OrderRepository;
import com.restaurant.modules.order.service.OrderService;
import com.restaurant.modules.order.validator.OrderValidator;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.service.TableService;
import com.restaurant.modules.user.dto.response.UserBriefResponse;
import com.restaurant.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Điều phối đơn hàng. Khóa theo thứ tự đặt bàn → đơn → bàn: hàm nào cần cả đơn lẫn bàn khóa đơn trước rồi mới đổi bàn.
 * Tên bàn và tên nhân viên lấy theo lô qua {@link TableService} và {@link UserService}, không gọi từng dòng.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    // Dòng chưa phục vụ xong: còn việc cho Bếp hoặc phục vụ
    private static final Set<OrderItemStatus> UNSERVED_STATUSES =
            EnumSet.of(OrderItemStatus.PENDING, OrderItemStatus.COOKING, OrderItemStatus.READY);

    // Tên field client được phép sắp xếp (snake_case) -> thuộc tính entity
    private static final Map<String, String> SORTABLE_FIELDS = Map.of("opened_at", "openedAt", "closed_at", "closedAt");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderValidator orderValidator;
    private final OrderMapper orderMapper;
    private final TableService tableService;
    private final DishService dishService;
    private final UserService userService;
    private final Clock clock;

    @Override
    @Transactional
    public UUID openOrder(OpenOrderCommand command) {
        if (tableService.getTableBriefs(List.of(command.tableId())).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bàn.");
        }
        Set<TableStatus> openableFrom = command.allowReservedTable()
                ? Set.of(TableStatus.AVAILABLE, TableStatus.RESERVED)
                : Set.of(TableStatus.AVAILABLE);
        // Câu UPDATE có điều kiện nên hai người cùng mở một bàn thì chỉ một người đổi được trạng thái
        if (!tableService.transitionStatus(command.tableId(), openableFrom, TableStatus.OCCUPIED)) {
            throw new BusinessException(ErrorCode.ORDER_TABLE_NOT_OPENABLE);
        }

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setTableId(command.tableId());
        order.setReservationId(command.reservationId());
        order.setCustomerId(command.customerId());
        order.setWaiterId(command.waiterId());
        order.setStatus(OrderStatus.OPEN);
        order.setOpenedAt(clock.instant());
        try {
            orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException e) {
            // Unique một phần uq_orders_open_table: bàn đã có đơn mở dù trạng thái bàn lệch
            throw new BusinessException(ErrorCode.ORDER_TABLE_NOT_OPENABLE);
        }
        return order.getId();
    }

    @Override
    @Transactional
    public OrderResponse openOrderForWalkIn(UUID waiterId, OrderOpenRequest request) {
        UUID orderId = openOrder(new OpenOrderCommand(request.tableId(), null, null, waiterId, false));
        return getOrder(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> searchOrders(OrderSearchRequest request, PageRequestParams params) {
        OrderStatus status = request.status() != null ? request.status() : OrderStatus.OPEN;
        Specification<Order> spec = (root, query, cb) -> cb.equal(root.get("status"), status);
        if (request.tableId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tableId"), request.tableId()));
        }
        Pageable pageable = params.toPageable(SORTABLE_FIELDS, "openedAt");
        Page<Order> page = orderRepository.findAll(spec, pageable);
        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Order> orders = page.getContent();
        Map<UUID, List<OrderItem>> itemsByOrder = orderItemRepository
                .findByOrderIdIn(orders.stream().map(Order::getId).toList()).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        Map<UUID, TableBriefResponse> tables = tableService
                .getTableBriefs(orders.stream().map(Order::getTableId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(TableBriefResponse::id, Function.identity()));
        Map<UUID, UserBriefResponse> waiters = userService
                .getUserBriefs(orders.stream().map(Order::getWaiterId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(UserBriefResponse::id, Function.identity()));

        return page.map(order -> {
            List<OrderItem> items = itemsByOrder.getOrDefault(order.getId(), List.of());
            UserBriefResponse waiter = waiters.get(order.getWaiterId());
            return new OrderSummaryResponse(order.getId(), tables.get(order.getTableId()), order.getStatus(),
                    waiter != null ? waiter.fullName() : null, order.getOpenedAt(), items.size(), subtotal(items));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id).orElseThrow(OrderServiceImpl::orderNotFound);
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderBySentAtAscIdAsc(id);
        List<TableBriefResponse> tables = tableService.getTableBriefs(Set.of(order.getTableId()));
        List<UserBriefResponse> waiters = userService.getUserBriefs(Set.of(order.getWaiterId()));
        List<OrderItemResponse> itemResponses = items.stream().map(orderMapper::toItemResponse).toList();
        return new OrderResponse(order.getId(), tables.isEmpty() ? null : tables.get(0), order.getStatus(),
                order.getReservationId(), waiters.isEmpty() ? null : waiters.get(0), order.getOpenedAt(), order.getClosedAt(),
                itemResponses, subtotal(items), unservedCount(items));
    }

    @Override
    @Transactional
    public OrderResponse addItems(UUID id, OrderItemsAddRequest request) {
        Order order = loadForUpdate(id);
        orderValidator.validateOpen(order);

        List<UUID> dishIds = request.items().stream().map(OrderItemLineRequest::dishId).distinct().toList();
        List<DishOrderingView> dishes = dishService.getDishesForOrdering(dishIds);
        orderValidator.validateDishesOrderable(dishes);

        Map<UUID, DishOrderingView> dishById = dishes.stream().collect(Collectors.toMap(DishOrderingView::id, Function.identity()));
        Instant now = clock.instant();
        List<OrderItem> items = request.items().stream()
                .map(line -> newItem(order.getId(), dishById.get(line.dishId()), line, now))
                .toList();
        orderItemRepository.saveAll(items);
        return getOrder(id);
    }

    @Override
    @Transactional
    public void cancelEmptyOrder(UUID id) {
        Order order = loadForUpdate(id);
        orderValidator.validateCancellable(order, orderItemRepository.existsByOrderId(id));

        orderRepository.delete(order);
        tableService.transitionStatus(order.getTableId(), Set.of(TableStatus.OCCUPIED), TableStatus.AVAILABLE);
    }

    @Override
    @Transactional
    public OrderPaymentView lockOrderForPayment(UUID id) {
        Order order = loadForUpdate(id);
        orderValidator.validateOpen(order);

        List<OrderItem> items = orderItemRepository.findByOrderIdOrderBySentAtAscIdAsc(id);
        List<PaymentLine> lines = items.stream().map(orderMapper::toPaymentLine).toList();
        return new OrderPaymentView(order.getId(), order.getTableId(), order.getReservationId(), order.getCustomerId(),
                order.getWaiterId(), order.getOpenedAt(), lines, unservedCount(items));
    }

    @Override
    @Transactional
    public void closeOrder(UUID id) {
        Order order = loadForUpdate(id);
        orderValidator.validateOpen(order);
        order.setStatus(OrderStatus.CLOSED);
        order.setClosedAt(clock.instant());
    }

    private Order loadForUpdate(UUID id) {
        return orderRepository.findByIdForUpdate(id).orElseThrow(OrderServiceImpl::orderNotFound);
    }

    private static BusinessException orderNotFound() {
        return new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy đơn hàng.");
    }

    // Tên món và giá chụp lại để đổi giá hay đổi tên món sau đó không ảnh hưởng đơn đang mở
    private OrderItem newItem(UUID orderId, DishOrderingView dish, OrderItemLineRequest line, Instant now) {
        OrderItem item = new OrderItem();
        item.setId(UUID.randomUUID());
        item.setOrderId(orderId);
        item.setDishId(dish.id());
        item.setDishName(dish.name());
        item.setUnitPrice(dish.price());
        item.setQuantity(line.quantity());
        item.setNote(line.note());
        item.setStatus(OrderItemStatus.PENDING);
        item.setSentAt(now);
        return item;
    }

    private static long subtotal(Collection<OrderItem> items) {
        return items.stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELLED)
                .mapToLong(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }

    private static int unservedCount(Collection<OrderItem> items) {
        return (int) items.stream().filter(item -> UNSERVED_STATUSES.contains(item.getStatus())).count();
    }
}
