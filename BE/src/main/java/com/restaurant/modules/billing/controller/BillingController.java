package com.restaurant.modules.billing.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.modules.billing.dto.response.PaymentSummaryResponse;
import com.restaurant.modules.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Màn hình thanh toán (UC018): Thu ngân xem tạm tính của đơn trước khi trả. */
@RestController
@RequestMapping("/billing")
@PreAuthorize("hasRole('CASHIER')")
@RequiredArgsConstructor
public class BillingController {

    private final InvoiceService invoiceService;

    @GetMapping("/orders/{orderId}")
    public ApiResponse<PaymentSummaryResponse> getPaymentSummary(@PathVariable UUID orderId) {
        return ApiResponse.of(invoiceService.getPaymentSummary(orderId));
    }
}
