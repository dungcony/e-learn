package com.restaurant.modules.order.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Một món khách gọi.
 *
 * @param quantity số lượng từ 1 đến 99
 * @param note     ghi chú cho bếp, tối đa 255 ký tự (vd "không hành, ít cay")
 */
public record OrderItemLineRequest(
        @NotNull(message = "Món không được để trống")
        UUID dishId,

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        @Max(value = 99, message = "Số lượng tối đa là 99")
        Integer quantity,

        @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
        String note) {
}
