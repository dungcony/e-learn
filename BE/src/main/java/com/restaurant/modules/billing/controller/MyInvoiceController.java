package com.restaurant.modules.billing.controller;

import com.restaurant.common.response.ApiResponse;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.response.PageResponse;
import com.restaurant.common.security.SecurityContextUtil;
import com.restaurant.modules.billing.dto.response.InvoiceResponse;
import com.restaurant.modules.billing.dto.response.InvoiceSummaryResponse;
import com.restaurant.modules.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Hóa đơn của chính khách hàng đã đăng nhập (UC017). */
@RestController
@RequestMapping("/me/invoices")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class MyInvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ApiResponse<PageResponse<InvoiceSummaryResponse>> listMine(PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(invoiceService.listMine(SecurityContextUtil.currentUserId(), params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> getMine(@PathVariable UUID id) {
        return ApiResponse.of(invoiceService.getMine(SecurityContextUtil.currentUserId(), id));
    }
}
