package com.elearning.course.validator;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CourseValidator {

    /**
     * @throws BusinessException {@code COURSE_DATE_RANGE_INVALID} nếu {@code endDate} không sau {@code startDate}
     */
    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (!endDate.isAfter(startDate)) {
            throw new BusinessException(ErrorCode.COURSE_DATE_RANGE_INVALID);
        }
    }
}
