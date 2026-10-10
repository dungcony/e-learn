package com.restaurant.modules.billing.service;

import com.restaurant.modules.billing.dto.request.ReportRangeRequest;
import com.restaurant.modules.billing.dto.request.TopDishesRequest;
import com.restaurant.modules.billing.dto.response.RevenueReportResponse;
import com.restaurant.modules.billing.dto.response.TopDishesResponse;

/** Báo cáo doanh thu và món bán chạy (UC020) từ các hóa đơn {@code PAID}, tính theo múi giờ nhà hàng. */
public interface ReportService {

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code REPORT_RANGE_INVALID}
     */
    RevenueReportResponse getDailyRevenue(ReportRangeRequest request);

    /**
     * Gộp theo tháng lịch; tháng đầu và cuối chỉ tính hóa đơn nằm trong khoảng.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code REPORT_RANGE_INVALID}
     */
    RevenueReportResponse getMonthlyRevenue(ReportRangeRequest request);

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code REPORT_RANGE_INVALID}
     */
    TopDishesResponse getTopDishes(TopDishesRequest request);
}
