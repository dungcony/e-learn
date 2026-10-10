package com.restaurant.modules.order.service;

import com.restaurant.modules.order.dto.response.KitchenItemResponse;
import com.restaurant.modules.order.dto.response.OrderItemResponse;
import com.restaurant.modules.order.dto.response.ReadyItemResponse;
import com.restaurant.modules.order.enums.OrderItemStatus;

import java.util.List;
import java.util.UUID;

/**
 * Tiến độ chế biến và phục vụ từng dòng món (UC016, và phần xác nhận đã phục vụ của UC015). Mỗi bước đổi trạng thái chỉ thành công
 * khi dòng đang đúng trạng thái trước đó và đơn còn mở.
 */
public interface OrderItemService {

    /**
     * Hàng đợi bếp: dòng {@code PENDING} và {@code COOKING} của các đơn đang mở, gửi sớm nhất trước, tối đa 200 dòng.
     *
     * @param status chỉ lấy một trạng thái; {@code null} lấy cả hai
     * @throws com.restaurant.common.exception.BusinessException {@code VALIDATION_ERROR} nếu {@code status} khác hai trạng thái trên
     */
    List<KitchenItemResponse> getKitchenQueue(OrderItemStatus status);

    /**
     * Bếp nhận chế biến: {@code PENDING} → {@code COOKING}.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code ORDER_NOT_OPEN},
     *                                                          {@code ORDER_ITEM_STATUS_CONFLICT} nếu dòng đã được xử lý
     */
    KitchenItemResponse startCooking(UUID itemId, UUID chefId);

    /**
     * Bếp hoàn thành: {@code COOKING} → {@code READY}.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code ORDER_NOT_OPEN}, {@code ORDER_ITEM_STATUS_CONFLICT}
     */
    KitchenItemResponse markReady(UUID itemId);

    /** Món đã xong của các đơn do nhân viên này phụ trách, xong sớm nhất trước, tối đa 200 dòng. */
    List<ReadyItemResponse> getReadyItems(UUID waiterId);

    /**
     * Nhân viên phục vụ xác nhận đã mang món ra: {@code READY} → {@code SERVED}. Mọi nhân viên phục vụ đều bấm được.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code ORDER_NOT_OPEN}, {@code ORDER_ITEM_STATUS_CONFLICT}
     */
    OrderItemResponse markServed(UUID itemId);
}
