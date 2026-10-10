package com.restaurant.modules.billing.dto.response;

import java.util.List;

/** Báo cáo doanh thu; ngày hoặc tháng không có hóa đơn thì không có điểm, client tự điền 0 khi vẽ biểu đồ. */
public record RevenueReportResponse(List<RevenuePoint> points, RevenueSummary summary) {
}
