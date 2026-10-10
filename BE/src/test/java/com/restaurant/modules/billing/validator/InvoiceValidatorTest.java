package com.restaurant.modules.billing.validator;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.modules.billing.enums.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceValidatorTest {

    private final InvoiceValidator validator = new InvoiceValidator();

    private void assertCode(Runnable action, String code) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class).extracting("code").isEqualTo(code);
    }

    @Test
    void validateOrderPayable_rejects_an_order_without_chargeable_lines() {
        assertCode(() -> validator.validateOrderPayable(0, 0, false), "INVOICE_ORDER_EMPTY");
    }

    @Test
    void validateOrderPayable_warns_about_unserved_lines_until_the_cashier_confirms() {
        assertThatThrownBy(() -> validator.validateOrderPayable(3, 2, false))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("INVOICE_UNSERVED_ITEMS");
                    assertThat(e.getDetail()).isEqualTo(java.util.Map.of("unserved_count", 2));
                });
        assertThatCode(() -> validator.validateOrderPayable(3, 2, true)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validateOrderPayable(3, 0, false)).doesNotThrowAnyException();
    }

    @Test
    void validatePayment_cash_needs_an_amount_covering_the_total() {
        assertCode(() -> validator.validatePayment(PaymentMethod.CASH, null, 172800), "VALIDATION_ERROR");
        assertCode(() -> validator.validatePayment(PaymentMethod.CASH, 172799L, 172800), "INVOICE_AMOUNT_INSUFFICIENT");
        assertThatCode(() -> validator.validatePayment(PaymentMethod.CASH, 172800L, 172800)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validatePayment(PaymentMethod.CASH, 500000L, 172800)).doesNotThrowAnyException();
    }

    @Test
    void validatePayment_card_and_transfer_ignore_the_amount_received() {
        assertThatCode(() -> validator.validatePayment(PaymentMethod.CARD, null, 172800)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validatePayment(PaymentMethod.BANK_TRANSFER, 1L, 172800)).doesNotThrowAnyException();
    }

    @Test
    void validateSearchRange_rejects_from_after_to() {
        assertCode(() -> validator.validateSearchRange(LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 1)), "VALIDATION_ERROR");
        assertThatCode(() -> validator.validateSearchRange(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 1))).doesNotThrowAnyException();
        assertThatCode(() -> validator.validateSearchRange(null, LocalDate.of(2026, 10, 1))).doesNotThrowAnyException();
    }
}
