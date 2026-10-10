package com.restaurant.modules.billing.helper;

import com.restaurant.modules.order.dto.response.PaymentLine;
import com.restaurant.modules.order.enums.OrderItemStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceCalculatorTest {

    private final InvoiceCalculator calculator = new InvoiceCalculator();
    private final BigDecimal vat8 = new BigDecimal("0.08");

    private PaymentLine line(long price, int qty, OrderItemStatus status) {
        return new PaymentLine(UUID.randomUUID(), "Món", price, qty, status);
    }

    @Test
    void calculate_sums_lines_and_adds_vat() {
        InvoiceAmounts amounts = calculator.calculate(
                List.of(line(65000, 2, OrderItemStatus.SERVED), line(30000, 1, OrderItemStatus.SERVED)), vat8);

        assertThat(amounts.subtotal()).isEqualTo(160000L);
        assertThat(amounts.vatAmount()).isEqualTo(12800L);
        assertThat(amounts.totalAmount()).isEqualTo(172800L);
    }

    @Test
    void calculate_rounds_vat_half_up_to_whole_dong() {
        // 33.333 × 8% = 2.666,64 → 2.667
        InvoiceAmounts amounts = calculator.calculate(List.of(line(33333, 1, OrderItemStatus.SERVED)), vat8);

        assertThat(amounts.vatAmount()).isEqualTo(2667L);
        assertThat(amounts.totalAmount()).isEqualTo(36000L);
    }

    @Test
    void calculate_rounds_exact_half_up() {
        // 1.250 × 10% = 125 (đúng); 1.255 × 10% = 125,5 → 126
        assertThat(calculator.calculate(List.of(line(1250, 1, OrderItemStatus.SERVED)), new BigDecimal("0.10")).vatAmount()).isEqualTo(125L);
        assertThat(calculator.calculate(List.of(line(1255, 1, OrderItemStatus.SERVED)), new BigDecimal("0.10")).vatAmount()).isEqualTo(126L);
    }

    @Test
    void calculate_ignores_cancelled_lines() {
        InvoiceAmounts amounts = calculator.calculate(
                List.of(line(65000, 1, OrderItemStatus.SERVED), line(99000, 3, OrderItemStatus.CANCELLED)), vat8);

        assertThat(amounts.subtotal()).isEqualTo(65000L);
    }

    @Test
    void calculate_counts_lines_not_yet_served_because_the_cashier_may_choose_to_charge_them() {
        InvoiceAmounts amounts = calculator.calculate(List.of(line(5000, 2, OrderItemStatus.PENDING)), vat8);

        assertThat(amounts.subtotal()).isEqualTo(10000L);
    }

    @Test
    void calculate_with_no_lines_is_all_zero() {
        InvoiceAmounts amounts = calculator.calculate(List.of(), vat8);

        assertThat(amounts.subtotal()).isZero();
        assertThat(amounts.vatAmount()).isZero();
        assertThat(amounts.totalAmount()).isZero();
    }
}
