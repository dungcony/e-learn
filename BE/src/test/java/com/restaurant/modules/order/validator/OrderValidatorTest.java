package com.restaurant.modules.order.validator;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.modules.menu.dto.response.DishOrderingView;
import com.restaurant.modules.menu.enums.DishStatus;
import com.restaurant.modules.order.entity.Order;
import com.restaurant.modules.order.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderValidatorTest {

    private final OrderValidator validator = new OrderValidator();

    private Order order(OrderStatus status) {
        Order o = new Order();
        o.setId(UUID.randomUUID());
        o.setStatus(status);
        return o;
    }

    @Test
    void validateOpen_accepts_open_order_and_rejects_closed_one() {
        assertThatCode(() -> validator.validateOpen(order(OrderStatus.OPEN))).doesNotThrowAnyException();
        assertThatThrownBy(() -> validator.validateOpen(order(OrderStatus.CLOSED)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_NOT_OPEN");
    }

    @Test
    void validateDishesOrderable_accepts_available_dishes() {
        List<DishOrderingView> dishes = List.of(
                new DishOrderingView(UUID.randomUUID(), "Phở bò", 65000, DishStatus.AVAILABLE),
                new DishOrderingView(UUID.randomUUID(), "Trà đá", 5000, DishStatus.AVAILABLE));

        assertThatCode(() -> validator.validateDishesOrderable(dishes)).doesNotThrowAnyException();
    }

    @Test
    void validateDishesOrderable_lists_every_out_of_stock_or_discontinued_dish() {
        DishOrderingView ok = new DishOrderingView(UUID.randomUUID(), "Phở bò", 65000, DishStatus.AVAILABLE);
        DishOrderingView out = new DishOrderingView(UUID.randomUUID(), "Chè", 30000, DishStatus.OUT_OF_STOCK);
        DishOrderingView stopped = new DishOrderingView(UUID.randomUUID(), "Lẩu", 250000, DishStatus.DISCONTINUED);

        assertThatThrownBy(() -> validator.validateDishesOrderable(List.of(ok, out, stopped)))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("DISH_NOT_ORDERABLE");
                    assertThat(e.getDetail()).isEqualTo(java.util.Map.of("dish_ids", List.of(out.id(), stopped.id())));
                });
    }

    @Test
    void validateCancellable_requires_an_open_order_without_items() {
        assertThatCode(() -> validator.validateCancellable(order(OrderStatus.OPEN), false)).doesNotThrowAnyException();
        assertThatThrownBy(() -> validator.validateCancellable(order(OrderStatus.OPEN), true))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_HAS_ITEMS");
        assertThatThrownBy(() -> validator.validateCancellable(order(OrderStatus.CLOSED), false))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_NOT_OPEN");
    }
}
