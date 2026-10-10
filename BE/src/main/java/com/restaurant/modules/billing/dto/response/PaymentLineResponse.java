package com.restaurant.modules.billing.dto.response;

import com.restaurant.modules.order.enums.OrderItemStatus;

/**
 * Một dòng món trên màn hình thanh toán.
 *
 * @param unitPrice giá chụp lúc gửi bếp, VND
 * @param status    trạng thái dòng món để Thu ngân thấy món nào chưa phục vụ
 */
public record PaymentLineResponse(String dishName, long unitPrice, int quantity, long lineTotal, OrderItemStatus status) {
}
