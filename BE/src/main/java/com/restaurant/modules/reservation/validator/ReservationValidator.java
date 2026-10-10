package com.restaurant.modules.reservation.validator;

import com.restaurant.common.config.RestaurantProperties;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.modules.reservation.entity.Reservation;
import com.restaurant.modules.reservation.enums.ReservationStatus;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.enums.TableStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

/** Quy tắc nghiệp vụ của đặt bàn: giờ hợp lệ, bàn phù hợp, được phép hủy, trạng thái (rule 2.6, tầng 2). */
@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final RestaurantProperties properties;
    private final Clock clock;

    /**
     * Giờ hẹn hợp lệ khi nằm trong giờ mở cửa, cách hiện tại ít nhất {@code booking-min-lead} và nhiều nhất
     * {@code booking-max-ahead-days} ngày. Giờ so theo múi giờ của {@link Clock}.
     */
    public boolean isBookableTime(Instant reservedAt) {
        LocalTime time = reservedAt.atZone(clock.getZone()).toLocalTime();
        boolean insideOpeningHours = !time.isBefore(properties.openTime()) && time.isBefore(properties.closeTime());
        Instant now = clock.instant();
        boolean farEnough = !reservedAt.isBefore(now.plus(properties.bookingMinLead()));
        boolean notTooFar = !reservedAt.isAfter(now.plus(Duration.ofDays(properties.bookingMaxAheadDays())));
        return insideOpeningHours && farEnough && notTooFar;
    }

    /**
     * @throws BusinessException {@code RESERVATION_TIME_INVALID} nếu giờ hẹn không hợp lệ
     */
    public void validateReservationTime(Instant reservedAt) {
        if (!isBookableTime(reservedAt)) {
            throw new BusinessException(ErrorCode.RESERVATION_TIME_INVALID);
        }
    }

    /**
     * Bàn gán cho đặt bàn phải tồn tại, không tạm khóa và đủ sức chứa.
     *
     * @param table bàn tìm được; {@code null} nếu id không tồn tại
     * @throws BusinessException {@code VALIDATION_ERROR}
     */
    public void validateTableForReservation(Reservation reservation, TableBriefResponse table) {
        if (table == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Bàn không tồn tại.");
        }
        if (table.status() == TableStatus.OUT_OF_SERVICE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Bàn đang tạm khóa.");
        }
        if (table.capacity() < reservation.getGuestCount()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Bàn không đủ chỗ cho số khách của đặt bàn.");
        }
    }

    /**
     * Khách hàng chỉ tự hủy được khi còn ít nhất {@code customer-cancel-min-hours} giờ tới giờ hẹn.
     *
     * @throws BusinessException {@code RESERVATION_CANCEL_TOO_LATE}
     */
    public void validateCustomerCanCancel(Reservation reservation) {
        Instant deadline = reservation.getReservedAt().minus(Duration.ofHours(properties.customerCancelMinHours()));
        if (clock.instant().isAfter(deadline)) {
            throw new BusinessException(ErrorCode.RESERVATION_CANCEL_TOO_LATE);
        }
    }

    /**
     * @throws BusinessException {@code RESERVATION_STATUS_INVALID} nếu trạng thái hiện tại không nằm trong {@code allowed}
     */
    public void validateStatus(Reservation reservation, Set<ReservationStatus> allowed) {
        if (!allowed.contains(reservation.getStatus())) {
            throw new BusinessException(ErrorCode.RESERVATION_STATUS_INVALID);
        }
    }
}
