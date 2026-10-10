package com.restaurant.modules.order.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.modules.order.dto.response.KitchenItemResponse;
import com.restaurant.modules.order.dto.response.OrderItemResponse;
import com.restaurant.modules.order.dto.response.ReadyItemResponse;
import com.restaurant.modules.order.entity.Order;
import com.restaurant.modules.order.entity.OrderItem;
import com.restaurant.modules.order.enums.OrderItemStatus;
import com.restaurant.modules.order.enums.OrderStatus;
import com.restaurant.modules.order.mapper.OrderMapper;
import com.restaurant.modules.order.repository.OrderItemRepository;
import com.restaurant.modules.order.repository.OrderRepository;
import com.restaurant.modules.order.repository.QueueRow;
import com.restaurant.modules.order.service.OrderItemService;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mỗi bước đổi trạng thái là một câu UPDATE có điều kiện ở repository; khi nó không đổi được dòng nào, hàm ở đây tra tiếp để
 * phân biệt dòng không tồn tại, đơn đã đóng, hay dòng đã được người khác xử lý.
 */
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    // Hàng đợi bếp và danh sách món đã xong đều giới hạn 200 dòng
    private static final Pageable QUEUE_PAGE = PageRequest.of(0, 200);

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final TableService tableService;
    private final OrderMapper orderMapper;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<KitchenItemResponse> getKitchenQueue(OrderItemStatus status) {
        Set<OrderItemStatus> statuses;
        if (status == null) {
            statuses = Set.of(OrderItemStatus.PENDING, OrderItemStatus.COOKING);
        } else if (status == OrderItemStatus.PENDING || status == OrderItemStatus.COOKING) {
            statuses = Set.of(status);
        } else {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Hàng đợi bếp chỉ gồm món chờ chế biến và đang chế biến.");
        }
        List<QueueRow> rows = orderItemRepository.findQueue(statuses, QUEUE_PAGE);
        Map<UUID, String> tableNames = tableNames(rows);
        return rows.stream()
                .map(row -> new KitchenItemResponse(row.getId(), row.getOrderId(), tableNames.get(row.getTableId()),
                        row.getDishName(), row.getQuantity(), row.getNote(), row.getStatus(), row.getSentAt(), row.getStartedAt()))
                .toList();
    }

    @Override
    @Transactional
    public KitchenItemResponse startCooking(UUID itemId, UUID chefId) {
        if (orderItemRepository.markCooking(itemId, chefId, clock.instant()) == 0) {
            throw diagnoseNoChange(itemId);
        }
        return kitchenResponse(itemId);
    }

    @Override
    @Transactional
    public KitchenItemResponse markReady(UUID itemId) {
        if (orderItemRepository.markReady(itemId, clock.instant()) == 0) {
            throw diagnoseNoChange(itemId);
        }
        return kitchenResponse(itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadyItemResponse> getReadyItems(UUID waiterId) {
        List<QueueRow> rows = orderItemRepository.findReadyForWaiter(waiterId, QUEUE_PAGE);
        Map<UUID, String> tableNames = tableNames(rows);
        return rows.stream()
                .map(row -> new ReadyItemResponse(row.getId(), row.getOrderId(), tableNames.get(row.getTableId()),
                        row.getDishName(), row.getQuantity(), row.getNote(), row.getReadyAt()))
                .toList();
    }

    @Override
    @Transactional
    public OrderItemResponse markServed(UUID itemId) {
        if (orderItemRepository.markServed(itemId, clock.instant()) == 0) {
            throw diagnoseNoChange(itemId);
        }
        return orderMapper.toItemResponse(loadItem(itemId));
    }

    private KitchenItemResponse kitchenResponse(UUID itemId) {
        OrderItem item = loadItem(itemId);
        Order order = orderRepository.findById(item.getOrderId()).orElseThrow(OrderItemServiceImpl::itemNotFound);
        String tableName = tableService.getTableBriefs(Set.of(order.getTableId())).stream()
                .findFirst().map(TableBriefResponse::name).orElse(null);
        return new KitchenItemResponse(item.getId(), item.getOrderId(), tableName, item.getDishName(), item.getQuantity(),
                item.getNote(), item.getStatus(), item.getSentAt(), item.getStartedAt());
    }

    private Map<UUID, String> tableNames(List<QueueRow> rows) {
        if (rows.isEmpty()) {
            return Map.of();
        }
        Set<UUID> tableIds = rows.stream().map(QueueRow::getTableId).collect(Collectors.toSet());
        return tableService.getTableBriefs(tableIds).stream()
                .collect(Collectors.toMap(TableBriefResponse::id, TableBriefResponse::name, (a, b) -> a, java.util.HashMap::new));
    }

    // Câu UPDATE không đổi được dòng nào: tra xem do dòng không tồn tại, đơn đã đóng hay dòng không ở trạng thái mong đợi
    private BusinessException diagnoseNoChange(UUID itemId) {
        OrderItem item = orderItemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return itemNotFound();
        }
        Order order = orderRepository.findById(item.getOrderId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.OPEN) {
            return new BusinessException(ErrorCode.ORDER_NOT_OPEN);
        }
        return new BusinessException(ErrorCode.ORDER_ITEM_STATUS_CONFLICT);
    }

    private OrderItem loadItem(UUID itemId) {
        return orderItemRepository.findById(itemId).orElseThrow(OrderItemServiceImpl::itemNotFound);
    }

    private static BusinessException itemNotFound() {
        return new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy dòng món.");
    }
}
