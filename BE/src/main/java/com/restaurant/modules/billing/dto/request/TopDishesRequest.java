package com.restaurant.modules.billing.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.BindParam;

import java.time.LocalDate;

/**
 * Tham số báo cáo món bán chạy, nhận từ query string.
 *
 * @param topN số món tối đa trả về từ 1 đến 50; bỏ trống thì lấy 10
 */
public record TopDishesRequest(
        @BindParam("from_date") @NotNull(message = "Ngày bắt đầu không được để trống") LocalDate fromDate,
        @BindParam("to_date") @NotNull(message = "Ngày kết thúc không được để trống") LocalDate toDate,
        @BindParam("top_n") @Min(value = 1, message = "Số món tối thiểu là 1") @Max(value = 50, message = "Số món tối đa là 50") Integer topN) {
}
