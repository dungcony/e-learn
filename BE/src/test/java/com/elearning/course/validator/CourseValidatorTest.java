package com.elearning.course.validator;

import com.elearning.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseValidatorTest {

    private final CourseValidator validator = new CourseValidator();

    @Test
    void validateDateRange_endAfterStartPasses() {
        assertThatCode(() -> validator.validateDateRange(LocalDate.of(2020, 4, 15), LocalDate.of(2020, 4, 16)))
                .doesNotThrowAnyException();
    }

    @Test
    void validateDateRange_sameDayOrEarlierEndFails() {
        LocalDate start = LocalDate.of(2020, 4, 15);
        for (LocalDate end : new LocalDate[]{start, start.minusDays(1)}) {
            assertThatThrownBy(() -> validator.validateDateRange(start, end))
                    .isInstanceOfSatisfying(BusinessException.class,
                            e -> assertThat(e.getCode()).isEqualTo("COURSE_DATE_RANGE_INVALID"));
        }
    }
}
