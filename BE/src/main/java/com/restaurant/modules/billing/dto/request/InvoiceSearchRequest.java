package com.restaurant.modules.billing.dto.request;

import com.restaurant.modules.billing.enums.PaymentMethod;
import org.springframework.web.bind.annotation.BindParam;

import java.time.LocalDate;

/**
 * Tiêu chí tìm hóa đơn, nhận từ query string; kết hợp bằng AND.
 *
 * @param code     mã hóa đơn chứa chuỗi, không phân biệt hoa thường
 * @param fromDate ngày thanh toán từ (gồm cả ngày này) theo múi giờ nhà hàng
 * @param toDate   ngày thanh toán đến (gồm cả ngày này)
 * @param table    tên bàn chứa chuỗi
 */
public record InvoiceSearchRequest(
        String code,
        @BindParam("from_date") LocalDate fromDate,
        @BindParam("to_date") LocalDate toDate,
        String table,
        @BindParam("payment_method") PaymentMethod paymentMethod) {
}
