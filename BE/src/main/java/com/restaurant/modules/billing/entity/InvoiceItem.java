package com.restaurant.modules.billing.entity;

import com.restaurant.common.abstracts.AssignedIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Một dòng món của hóa đơn (bảng {@code invoice_items}), chụp tên và đơn giá lúc thanh toán; không gộp các dòng cùng món. */
@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceItem extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    // Thứ tự dòng trong hóa đơn, bắt đầu từ 0
    @Column(nullable = false)
    private int position;

    // Dùng cho báo cáo món bán chạy; không khóa ngoại sang module thực đơn
    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    @Column(name = "dish_name", nullable = false)
    private String dishName;

    @Column(name = "unit_price", nullable = false)
    private long unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_total", nullable = false)
    private long lineTotal;
}
