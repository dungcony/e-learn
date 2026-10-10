package com.restaurant.modules.order.validator;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.modules.menu.dto.response.DishOrderingView;
import com.restaurant.modules.menu.enums.DishStatus;
import com.restaurant.modules.order.entity.Order;
import com.restaurant.modules.order.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Kiểm tra nghiệp vụ của đơn hàng (rule 2.6, tầng 2). */
@Component
public class OrderValidator {

    /**
     * @throws BusinessException {@code ORDER_NOT_OPEN} nếu đơn đã đóng
     */
    public void validateOpen(Order order) {
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessException(ErrorCode.ORDER_NOT_OPEN);
        }
    }

    /**
     * Chỉ gọi được món đang bán; một món hỏng thì cả lần gọi bị từ chối để không gửi bếp dở dang.
     *
     * @throws BusinessException {@code DISH_NOT_ORDERABLE} kèm {@code detail.dish_ids} là các món hết hoặc ngừng bán
     */
    public void validateDishesOrderable(List<DishOrderingView> dishes) {
        List<UUID> rejected = dishes.stream()
                .filter(dish -> dish.status() != DishStatus.AVAILABLE)
                .map(DishOrderingView::id)
                .toList();
        if (!rejected.isEmpty()) {
            throw new BusinessException(ErrorCode.DISH_NOT_ORDERABLE, ErrorCode.DISH_NOT_ORDERABLE.getDefaultMessage(),
                    Map.of("dish_ids", rejected));
        }
    }

    /**
     * Chỉ hủy được đơn đang mở mà chưa có dòng món nào (mở bàn nhầm).
     *
     * @throws BusinessException {@code ORDER_NOT_OPEN} nếu đã đóng, {@code ORDER_HAS_ITEMS} nếu đã có món
     */
    public void validateCancellable(Order order, boolean hasItems) {
        validateOpen(order);
        if (hasItems) {
            throw new BusinessException(ErrorCode.ORDER_HAS_ITEMS);
        }
    }
}
