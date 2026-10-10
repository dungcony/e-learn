package com.restaurant.modules.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Gọi món và gửi bếp trong một lần; giỏ tạm nằm ở client. Cùng một món có thể xuất hiện ở nhiều dòng với ghi chú khác nhau.
 */
public record OrderItemsAddRequest(
        @NotEmpty(message = "Cần chọn ít nhất một món")
        @Size(max = 50, message = "Mỗi lần gọi tối đa 50 dòng món")
        List<@Valid OrderItemLineRequest> items) {
}
