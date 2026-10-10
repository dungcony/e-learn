package com.restaurant.modules.billing.entity;

import com.restaurant.common.abstracts.AssignedIdEntity;
import com.restaurant.modules.billing.enums.InvoiceStatus;
import com.restaurant.modules.billing.enums.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Hóa đơn (bảng {@code invoices}): bản chụp bất biến của đơn lúc thanh toán nên tên bàn, tên thu ngân và VAT được lưu thẳng,
 * không phụ thuộc dữ liệu của module khác về sau. Sau khi tạo chỉ {@code print_count} được tăng (khi in lại). Số tiền là VND.
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "table_id", nullable = false)
    private UUID tableId;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    // Sao chép từ đơn; null với khách vãng lai
    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "cashier_id", nullable = false)
    private UUID cashierId;

    @Column(name = "cashier_name", nullable = false)
    private String cashierName;

    @Column(nullable = false)
    private long subtotal;

    // Tỷ lệ VAT tại thời điểm trả, vd 0.0800
    @Column(name = "vat_rate", nullable = false)
    private BigDecimal vatRate;

    @Column(name = "vat_amount", nullable = false)
    private long vatAmount;

    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount_received", nullable = false)
    private long amountReceived;

    @Column(name = "change_amount", nullable = false)
    private long changeAmount;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;

    @Column(name = "print_count", nullable = false)
    private int printCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
