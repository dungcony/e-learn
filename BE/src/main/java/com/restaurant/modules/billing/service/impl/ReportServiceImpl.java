package com.restaurant.modules.billing.service.impl;

import com.restaurant.modules.billing.dto.request.ReportRangeRequest;
import com.restaurant.modules.billing.dto.request.TopDishesRequest;
import com.restaurant.modules.billing.dto.response.RevenuePoint;
import com.restaurant.modules.billing.dto.response.RevenueReportResponse;
import com.restaurant.modules.billing.dto.response.RevenueSummary;
import com.restaurant.modules.billing.dto.response.TopDishItem;
import com.restaurant.modules.billing.dto.response.TopDishesResponse;
import com.restaurant.modules.billing.repository.InvoiceReportRepository;
import com.restaurant.modules.billing.repository.RevenueRow;
import com.restaurant.modules.billing.service.ReportService;
import com.restaurant.modules.billing.validator.ReportValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Tổng hợp báo cáo bằng truy vấn gộp ở CSDL; ngày và tháng tính theo múi giờ của {@link Clock} (tức {@code app.timezone}). */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final int DEFAULT_TOP_N = 10;

    private final InvoiceReportRepository reportRepository;
    private final ReportValidator reportValidator;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getDailyRevenue(ReportRangeRequest request) {
        List<RevenueRow> rows = queryRevenue(request, reportRepository::sumByDay);
        return buildReport(rows, true);
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getMonthlyRevenue(ReportRangeRequest request) {
        List<RevenueRow> rows = queryRevenue(request, reportRepository::sumByMonth);
        return buildReport(rows, false);
    }

    @Override
    @Transactional(readOnly = true)
    public TopDishesResponse getTopDishes(TopDishesRequest request) {
        reportValidator.validateRange(request.fromDate(), request.toDate());
        int limit = request.topN() != null ? request.topN() : DEFAULT_TOP_N;
        List<TopDishItem> items = reportRepository.findTopDishes(startOf(request.fromDate()), startOf(request.toDate().plusDays(1)), limit)
                .stream()
                .map(row -> new TopDishItem(row.getDishId(), row.getDishName(), row.getQuantity(), row.getRevenue()))
                .toList();
        return new TopDishesResponse(items);
    }

    private List<RevenueRow> queryRevenue(ReportRangeRequest request, RevenueQuery query) {
        reportValidator.validateRange(request.fromDate(), request.toDate());
        return query.apply(startOf(request.fromDate()), startOf(request.toDate().plusDays(1)), clock.getZone().getId());
    }

    private RevenueReportResponse buildReport(List<RevenueRow> rows, boolean byDay) {
        List<RevenuePoint> points = rows.stream()
                .map(row -> new RevenuePoint(byDay ? row.getPeriod() : null, byDay ? null : row.getPeriod(), row.getInvoiceCount(),
                        row.getSubtotal(), row.getVatAmount(), row.getTotalAmount()))
                .toList();
        RevenueSummary summary = new RevenueSummary(
                points.stream().mapToLong(RevenuePoint::invoiceCount).sum(),
                points.stream().mapToLong(RevenuePoint::subtotal).sum(),
                points.stream().mapToLong(RevenuePoint::vatAmount).sum(),
                points.stream().mapToLong(RevenuePoint::totalAmount).sum());
        return new RevenueReportResponse(points, summary);
    }

    private Instant startOf(LocalDate date) {
        return date.atStartOfDay(clock.getZone()).toInstant();
    }

    @FunctionalInterface
    private interface RevenueQuery {
        List<RevenueRow> apply(Instant from, Instant to, String zone);
    }
}
