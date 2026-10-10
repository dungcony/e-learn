package com.restaurant.modules.billing.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.modules.billing.dto.request.ReportRangeRequest;
import com.restaurant.modules.billing.dto.request.TopDishesRequest;
import com.restaurant.modules.billing.dto.response.RevenueReportResponse;
import com.restaurant.modules.billing.dto.response.TopDishesResponse;
import com.restaurant.modules.billing.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Báo cáo doanh thu và món bán chạy cho Quản lý (UC020). */
@RestController
@RequestMapping("/reports")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/revenue/daily")
    public ApiResponse<RevenueReportResponse> getDailyRevenue(@Valid ReportRangeRequest request) {
        return ApiResponse.of(reportService.getDailyRevenue(request));
    }

    @GetMapping("/revenue/monthly")
    public ApiResponse<RevenueReportResponse> getMonthlyRevenue(@Valid ReportRangeRequest request) {
        return ApiResponse.of(reportService.getMonthlyRevenue(request));
    }

    @GetMapping("/top-dishes")
    public ApiResponse<TopDishesResponse> getTopDishes(@Valid TopDishesRequest request) {
        return ApiResponse.of(reportService.getTopDishes(request));
    }
}
