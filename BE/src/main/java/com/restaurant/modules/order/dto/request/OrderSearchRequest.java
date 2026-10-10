package com.restaurant.modules.order.dto.request;

import com.restaurant.modules.order.enums.OrderStatus;
import org.springframework.web.bind.annotation.BindParam;

import java.util.UUID;

/**
 * Tiêu chí tìm đơn hàng, nhận từ query string.
 *
 * @param status  trạng thái đơn; bỏ trống thì mặc định {@code OPEN}
 * @param tableId chỉ lấy đơn của bàn này
 */
public record OrderSearchRequest(OrderStatus status, @BindParam("table_id") UUID tableId) {
}
