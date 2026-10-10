package com.restaurant.modules.order.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.common.security.SecurityContextUtil;
import com.restaurant.modules.order.dto.response.KitchenItemResponse;
import com.restaurant.modules.order.enums.OrderItemStatus;
import com.restaurant.modules.order.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Màn hình bếp (UC016): xem hàng đợi, nhận chế biến, hoàn thành món; chỉ Bếp. Client gọi lại hàng đợi khoảng 5 giây một lần. */
@RestController
@RequestMapping("/kitchen")
@PreAuthorize("hasRole('CHEF')")
@RequiredArgsConstructor
public class KitchenController {

    private final OrderItemService orderItemService;

    @GetMapping("/items")
    public ApiResponse<List<KitchenItemResponse>> getQueue(@RequestParam(required = false) OrderItemStatus status) {
        return ApiResponse.of(orderItemService.getKitchenQueue(status));
    }

    @PutMapping("/items/{id}/start")
    public ApiResponse<KitchenItemResponse> startCooking(@PathVariable UUID id) {
        return ApiResponse.of(orderItemService.startCooking(id, SecurityContextUtil.currentUserId()));
    }

    @PutMapping("/items/{id}/ready")
    public ApiResponse<KitchenItemResponse> markReady(@PathVariable UUID id) {
        return ApiResponse.of(orderItemService.markReady(id));
    }
}
