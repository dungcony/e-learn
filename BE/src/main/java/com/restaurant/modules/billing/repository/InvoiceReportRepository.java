package com.restaurant.modules.billing.repository;

import com.restaurant.modules.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Truy vấn tổng hợp cho báo cáo; chỉ đọc. Khoảng thời gian là {@code [from, to)}. Nhóm theo ngày/tháng địa phương của
 * {@code zone} (tên múi giờ IANA) để hóa đơn lúc 23:50 giờ Việt Nam không bị tính sang ngày UTC.
 */
public interface InvoiceReportRepository extends JpaRepository<Invoice, UUID> {

    @Query(value = """
            SELECT to_char((paid_at AT TIME ZONE :zone)::date, 'YYYY-MM-DD') AS "period",
                   count(*)                                                  AS "invoiceCount",
                   CAST(sum(subtotal) AS bigint)                             AS "subtotal",
                   CAST(sum(vat_amount) AS bigint)                           AS "vatAmount",
                   CAST(sum(total_amount) AS bigint)                         AS "totalAmount"
            FROM invoices
            WHERE status = 'PAID' AND paid_at >= :from AND paid_at < :to
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<RevenueRow> sumByDay(@Param("from") Instant from, @Param("to") Instant to, @Param("zone") String zone);

    @Query(value = """
            SELECT to_char(paid_at AT TIME ZONE :zone, 'YYYY-MM') AS "period",
                   count(*)                                       AS "invoiceCount",
                   CAST(sum(subtotal) AS bigint)                  AS "subtotal",
                   CAST(sum(vat_amount) AS bigint)                AS "vatAmount",
                   CAST(sum(total_amount) AS bigint)              AS "totalAmount"
            FROM invoices
            WHERE status = 'PAID' AND paid_at >= :from AND paid_at < :to
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<RevenueRow> sumByMonth(@Param("from") Instant from, @Param("to") Instant to, @Param("zone") String zone);

    // Xếp theo số lượng giảm dần rồi doanh thu giảm dần; dish_name lấy max vì món có thể từng đổi tên giữa các hóa đơn
    @Query(value = """
            SELECT ii.dish_id                          AS "dishId",
                   max(ii.dish_name)                   AS "dishName",
                   CAST(sum(ii.quantity) AS bigint)    AS "quantity",
                   CAST(sum(ii.line_total) AS bigint)  AS "revenue"
            FROM invoice_items ii
            JOIN invoices i ON i.id = ii.invoice_id
            WHERE i.status = 'PAID' AND i.paid_at >= :from AND i.paid_at < :to
            GROUP BY ii.dish_id
            ORDER BY sum(ii.quantity) DESC, sum(ii.line_total) DESC, ii.dish_id
            LIMIT :limit
            """, nativeQuery = true)
    List<TopDishRow> findTopDishes(@Param("from") Instant from, @Param("to") Instant to, @Param("limit") int limit);
}
