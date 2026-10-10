package com.restaurant.modules.order.service;

import com.restaurant.common.response.PageRequestParams;
import com.restaurant.modules.order.dto.request.OpenOrderCommand;
import com.restaurant.modules.order.dto.request.OrderItemsAddRequest;
import com.restaurant.modules.order.dto.request.OrderOpenRequest;
import com.restaurant.modules.order.dto.request.OrderSearchRequest;
import com.restaurant.modules.order.dto.response.OrderPaymentView;
import com.restaurant.modules.order.dto.response.OrderResponse;
import com.restaurant.modules.order.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Đơn hàng của bàn (UC015) và API cho module đặt bàn (mở đơn lúc nhận bàn) và thanh toán (khóa rồi đóng đơn).
 * Mỗi bàn có tối đa một đơn {@code OPEN}.
 */
public interface OrderService {

    /**
     * Mở đơn: chuyển bàn sang {@code OCCUPIED} rồi tạo đơn {@code OPEN}, trong cùng transaction của người gọi.
     *
     * @return id đơn vừa mở
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND} nếu bàn không tồn tại,
     *                                                          {@code ORDER_TABLE_NOT_OPENABLE} nếu bàn không ở trạng thái cho phép
     */
    UUID openOrder(OpenOrderCommand command);

    /**
     * Mở đơn cho khách vãng lai ở bàn {@code AVAILABLE}.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code ORDER_TABLE_NOT_OPENABLE}
     */
    OrderResponse openOrderForWalkIn(UUID waiterId, OrderOpenRequest request);

    /** Danh sách đơn theo trạng thái (mặc định {@code OPEN}) và bàn. */
    Page<OrderSummaryResponse> searchOrders(OrderSearchRequest request, PageRequestParams params);

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}
     */
    OrderResponse getOrder(UUID id);

    /**
     * Gọi món và gửi bếp: lưu các dòng ở {@code PENDING} với tên và giá chụp tại thời điểm này. Một món không gọi được thì
     * không dòng nào được lưu.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code ORDER_NOT_OPEN},
     *                                                          {@code DISH_NOT_ORDERABLE}
     */
    OrderResponse addItems(UUID id, OrderItemsAddRequest request);

    /**
     * Hủy đơn mở nhầm chưa có dòng món và trả bàn về {@code AVAILABLE}.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code ORDER_NOT_OPEN}, {@code ORDER_HAS_ITEMS}
     */
    void cancelEmptyOrder(UUID id);

    /**
     * Khóa đơn ({@code FOR UPDATE}) và trả dữ liệu để tính tiền. Phải gọi trong transaction của người thanh toán; giữ khóa tới
     * khi transaction kết thúc nên không gọi I/O ngoài sau hàm này.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code ORDER_NOT_OPEN}
     */
    OrderPaymentView lockOrderForPayment(UUID id);

    /**
     * Đóng đơn sau khi đã thanh toán; không đụng tới bàn (module thanh toán tự trả bàn).
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code ORDER_NOT_OPEN}
     */
    void closeOrder(UUID id);
}
