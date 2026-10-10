package com.restaurant.modules.billing.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.modules.billing.dto.request.ReportRangeRequest;
import com.restaurant.modules.billing.dto.request.TopDishesRequest;
import com.restaurant.modules.billing.dto.response.RevenueReportResponse;
import com.restaurant.modules.billing.dto.response.TopDishesResponse;
import com.restaurant.modules.billing.repository.InvoiceReportRepository;
import com.restaurant.modules.billing.repository.RevenueRow;
import com.restaurant.modules.billing.repository.TopDishRow;
import com.restaurant.modules.billing.validator.ReportValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    // Khoảng 01/10 → 09/10 đổi sang thời điểm theo giờ nhà hàng: nửa đêm ngày đầu tới nửa đêm sau ngày cuối
    private static final Instant FROM = Instant.parse("2026-09-30T17:00:00Z");
    private static final Instant TO = Instant.parse("2026-10-09T17:00:00Z");

    @Mock
    private InvoiceReportRepository reportRepository;

    private ReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReportServiceImpl(reportRepository, new ReportValidator(366), Clock.fixed(Instant.parse("2026-10-09T12:30:00Z"), VN));
    }

    private record Row(String period, long invoiceCount, long subtotal, long vatAmount, long totalAmount) implements RevenueRow {
        @Override
        public String getPeriod() {
            return period;
        }

        @Override
        public long getInvoiceCount() {
            return invoiceCount;
        }

        @Override
        public long getSubtotal() {
            return subtotal;
        }

        @Override
        public long getVatAmount() {
            return vatAmount;
        }

        @Override
        public long getTotalAmount() {
            return totalAmount;
        }
    }

    private record Dish(UUID dishId, String dishName, long quantity, long revenue) implements TopDishRow {
        @Override
        public UUID getDishId() {
            return dishId;
        }

        @Override
        public String getDishName() {
            return dishName;
        }

        @Override
        public long getQuantity() {
            return quantity;
        }

        @Override
        public long getRevenue() {
            return revenue;
        }
    }

    @Test
    void getDailyRevenue_maps_rows_to_points_and_sums_the_summary() {
        when(reportRepository.sumByDay(FROM, TO, "Asia/Ho_Chi_Minh")).thenReturn(List.of(
                new Row("2026-10-08", 34, 8120000, 649600, 8769600),
                new Row("2026-10-09", 12, 2950000, 236000, 3186000)));

        RevenueReportResponse report = service.getDailyRevenue(new ReportRangeRequest(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 9)));

        assertThat(report.points()).hasSize(2);
        assertThat(report.points().get(0).date()).isEqualTo("2026-10-08");
        assertThat(report.points().get(0).month()).isNull();
        assertThat(report.points().get(1).totalAmount()).isEqualTo(3186000L);
        assertThat(report.summary().invoiceCount()).isEqualTo(46L);
        assertThat(report.summary().subtotal()).isEqualTo(11070000L);
        assertThat(report.summary().vatAmount()).isEqualTo(885600L);
        assertThat(report.summary().totalAmount()).isEqualTo(11955600L);
    }

    @Test
    void getMonthlyRevenue_puts_the_period_in_month_instead_of_date() {
        Instant from = Instant.parse("2026-09-14T17:00:00Z");
        when(reportRepository.sumByMonth(from, TO, "Asia/Ho_Chi_Minh")).thenReturn(List.of(
                new Row("2026-09", 20, 1000000, 80000, 1080000),
                new Row("2026-10", 5, 500000, 40000, 540000)));

        RevenueReportResponse report = service.getMonthlyRevenue(new ReportRangeRequest(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 10, 9)));

        assertThat(report.points()).extracting(p -> p.month()).containsExactly("2026-09", "2026-10");
        assertThat(report.points()).extracting(p -> p.date()).containsOnlyNulls();
        assertThat(report.summary().totalAmount()).isEqualTo(1620000L);
    }

    @Test
    void getDailyRevenue_with_no_invoices_returns_empty_points_and_zero_summary() {
        when(reportRepository.sumByDay(FROM, TO, "Asia/Ho_Chi_Minh")).thenReturn(List.of());

        RevenueReportResponse report = service.getDailyRevenue(new ReportRangeRequest(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 9)));

        assertThat(report.points()).isEmpty();
        assertThat(report.summary().invoiceCount()).isZero();
        assertThat(report.summary().totalAmount()).isZero();
    }

    @Test
    void getDailyRevenue_rejects_an_invalid_range_before_querying() {
        assertThatThrownBy(() -> service.getDailyRevenue(new ReportRangeRequest(LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 1))))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("REPORT_RANGE_INVALID");
        verify(reportRepository, never()).sumByDay(any(), any(), any());
    }

    @Test
    void getTopDishes_defaults_to_ten_and_maps_rows() {
        UUID pho = UUID.randomUUID();
        when(reportRepository.findTopDishes(FROM, TO, 10)).thenReturn(List.of(new Dish(pho, "Phở bò", 120, 7800000)));

        TopDishesResponse response = service.getTopDishes(new TopDishesRequest(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 9), null));

        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.dishId()).isEqualTo(pho);
            assertThat(item.dishName()).isEqualTo("Phở bò");
            assertThat(item.quantity()).isEqualTo(120L);
            assertThat(item.revenue()).isEqualTo(7800000L);
        });
    }

    @Test
    void getTopDishes_passes_the_requested_limit() {
        when(reportRepository.findTopDishes(FROM, TO, 3)).thenReturn(List.of());

        assertThat(service.getTopDishes(new TopDishesRequest(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 9), 3)).items()).isEmpty();
    }
}
