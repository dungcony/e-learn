package com.restaurant.modules.billing.helper;

import com.restaurant.modules.order.dto.response.PaymentLine;
import com.restaurant.modules.order.enums.OrderItemStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Tính tiền hóa đơn; dùng chung cho xem trước và thanh toán để hai nơi luôn ra cùng một con số. Dòng {@code CANCELLED} không
 * tính; VAT làm tròn {@code HALF_UP} về đồng nguyên.
 */
@Component
public class InvoiceCalculator {

    public InvoiceAmounts calculate(List<PaymentLine> lines, BigDecimal vatRate) {
        long subtotal = lines.stream()
                .filter(line -> line.status() != OrderItemStatus.CANCELLED)
                .mapToLong(line -> line.unitPrice() * line.quantity())
                .sum();
        long vat = BigDecimal.valueOf(subtotal).multiply(vatRate).setScale(0, RoundingMode.HALF_UP).longValueExact();
        return new InvoiceAmounts(subtotal, vat, subtotal + vat);
    }
}
