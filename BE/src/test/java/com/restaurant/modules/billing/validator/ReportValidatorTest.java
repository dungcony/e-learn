package com.restaurant.modules.billing.validator;

import com.restaurant.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportValidatorTest {

    private final ReportValidator validator = new ReportValidator(366);

    @Test
    void validateRange_accepts_a_single_day_and_the_maximum_span() {
        LocalDate from = LocalDate.of(2026, 1, 1);

        assertThatCode(() -> validator.validateRange(from, from)).doesNotThrowAnyException();
        // 366 ngày tính cả hai đầu: 01/01/2026 → 01/01/2027 là 366 ngày
        assertThatCode(() -> validator.validateRange(from, LocalDate.of(2027, 1, 1))).doesNotThrowAnyException();
    }

    @Test
    void validateRange_rejects_to_before_from() {
        assertThatThrownBy(() -> validator.validateRange(LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 8)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("REPORT_RANGE_INVALID");
    }

    @Test
    void validateRange_rejects_a_span_over_the_limit() {
        LocalDate from = LocalDate.of(2026, 1, 1);

        assertThatThrownBy(() -> validator.validateRange(from, LocalDate.of(2027, 1, 2)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("REPORT_RANGE_INVALID");
        assertThatThrownBy(() -> validator.validateRange(from, from.plusDays(400)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("REPORT_RANGE_INVALID");
    }
}
