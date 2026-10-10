package com.restaurant.modules.reservation.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.response.PageResponse;
import com.restaurant.common.security.SecurityContextUtil;
import com.restaurant.modules.reservation.dto.response.ReservationResponse;
import com.restaurant.modules.reservation.dto.response.ReservationSummaryResponse;
import com.restaurant.modules.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Lịch sử đặt bàn và hủy đặt bàn của chính khách hàng đã đăng nhập (UC017). */
@RestController
@RequestMapping("/me/reservations")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class MyReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ApiResponse<PageResponse<ReservationSummaryResponse>> listMine(PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(reservationService.listMine(SecurityContextUtil.currentUserId(), params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> getMine(@PathVariable UUID id) {
        return ApiResponse.of(reservationService.getMine(SecurityContextUtil.currentUserId(), id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<ReservationResponse> cancelMine(@PathVariable UUID id) {
        return ApiResponse.of(reservationService.cancelMine(SecurityContextUtil.currentUserId(), id));
    }
}
