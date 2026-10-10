package com.restaurant.modules.order.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.common.security.SecurityContextUtil;
import com.restaurant.modules.order.dto.response.OrderItemResponse;
import com.restaurant.modules.order.dto.response.ReadyItemResponse;
import com.restaurant.modules.order.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Món Bếp đã xong và xác nhận đã phục vụ (UC015); chỉ nhân viên phục vụ. Client gọi lại danh sách khoảng 5 giây một lần. */
@RestController
@RequestMapping("/order-items")
@PreAuthorize("hasRole('WAITER')")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping("/ready")
    public ApiResponse<List<ReadyItemResponse>> getReadyItems() {
        return ApiResponse.of(orderItemService.getReadyItems(SecurityContextUtil.currentUserId()));
    }

    @PutMapping("/{id}/served")
    public ApiResponse<OrderItemResponse> markServed(@PathVariable UUID id) {
        return ApiResponse.of(orderItemService.markServed(id));
    }
}
