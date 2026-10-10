package com.restaurant.modules.billing.validator;

import com.restaurant.common.config.RestaurantProperties;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ReportValidator {

    private final int maxDays;

    @Autowired
    public ReportValidator(RestaurantProperties properties) {
        this(properties.reportMaxDays());
    }

    public ReportValidator(int maxDays) {
        this.maxDays = maxDays;
    }

    /**
     * @throws BusinessException {@code REPORT_RANGE_INVALID} nếu ngày kết thúc trước ngày bắt đầu hoặc khoảng (tính cả hai đầu)
     *                           vượt số ngày tối đa cấu hình
     */
    public void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (toDate.isBefore(fromDate) || ChronoUnit.DAYS.between(fromDate, toDate) + 1 > maxDays) {
            throw new BusinessException(ErrorCode.REPORT_RANGE_INVALID);
        }
    }
}
