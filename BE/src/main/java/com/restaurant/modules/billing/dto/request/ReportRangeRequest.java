package com.restaurant.modules.billing.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.BindParam;

import java.time.LocalDate;

/**
 * Khoảng thời gian của báo cáo doanh thu, nhận từ query string; gồm cả hai đầu theo múi giờ nhà hàng.
 *
 * @param fromDate ngày bắt đầu
 * @param toDate   ngày kết thúc; không trước {@code fromDate} và cách tối đa 366 ngày (kiểm ở validator)
 */
public record ReportRangeRequest(
        @BindParam("from_date") @NotNull(message = "Ngày bắt đầu không được để trống") LocalDate fromDate,
        @BindParam("to_date") @NotNull(message = "Ngày kết thúc không được để trống") LocalDate toDate) {
}
