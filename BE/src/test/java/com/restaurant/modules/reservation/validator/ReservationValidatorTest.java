package com.restaurant.modules.reservation.validator;

import com.restaurant.common.config.RestaurantProperties;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.modules.reservation.entity.Reservation;
import com.restaurant.modules.reservation.enums.ReservationStatus;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.enums.TableZone;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationValidatorTest {

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    // 09/10/2026 09:00 giờ Việt Nam
    private static final Instant NOW = Instant.parse("2026-10-09T02:00:00Z");

    private final RestaurantProperties properties = new RestaurantProperties(LocalTime.of(10, 0), LocalTime.of(22, 0),
            Duration.ofMinutes(120), Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2), 30, 2, 4,
            new BigDecimal("0.08"), 366);
    private final ReservationValidator validator = new ReservationValidator(properties, Clock.fixed(NOW, VN));

    private static Instant at(String vietnamLocal) {
        return java.time.LocalDateTime.parse(vietnamLocal).atZone(VN).toInstant();
    }

    private void assertCode(Runnable action, String code) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class).extracting("code").isEqualTo(code);
    }

    @Test
    void validateReservationTime_accepts_a_normal_evening_booking() {
        assertThatCode(() -> validator.validateReservationTime(at("2026-10-12T19:00:00"))).doesNotThrowAnyException();
    }

    @Test
    void validateReservationTime_accepts_opening_time_and_last_minute_before_closing() {
        assertThatCode(() -> validator.validateReservationTime(at("2026-10-12T10:00:00"))).doesNotThrowAnyException();
        assertThatCode(() -> validator.validateReservationTime(at("2026-10-12T21:59:00"))).doesNotThrowAnyException();
    }

    @Test
    void validateReservationTime_rejects_outside_opening_hours() {
        assertCode(() -> validator.validateReservationTime(at("2026-10-12T22:00:00")), "RESERVATION_TIME_INVALID");
        assertCode(() -> validator.validateReservationTime(at("2026-10-12T09:30:00")), "RESERVATION_TIME_INVALID");
    }

    @Test
    void validateReservationTime_requires_at_least_two_hours_notice() {
        assertCode(() -> validator.validateReservationTime(at("2026-10-09T10:30:00")), "RESERVATION_TIME_INVALID");
        assertThatCode(() -> validator.validateReservationTime(at("2026-10-09T11:00:00"))).doesNotThrowAnyException();
    }

    @Test
    void validateReservationTime_rejects_the_past() {
        assertCode(() -> validator.validateReservationTime(at("2026-10-08T19:00:00")), "RESERVATION_TIME_INVALID");
    }

    @Test
    void validateReservationTime_allows_booking_close_to_the_thirty_day_limit() {
        assertThatCode(() -> validator.validateReservationTime(at("2026-11-07T19:00:00"))).doesNotThrowAnyException();
    }

    @Test
    void validateReservationTime_rejects_more_than_thirty_days_ahead() {
        // 30 ngày sau là 08/11 lúc 09:00; 10:00 cùng ngày đã quá giới hạn
        assertCode(() -> validator.validateReservationTime(at("2026-11-08T10:00:00")), "RESERVATION_TIME_INVALID");
        assertCode(() -> validator.validateReservationTime(at("2026-11-09T19:00:00")), "RESERVATION_TIME_INVALID");
    }

    private Reservation reservation(int guests, Instant reservedAt) {
        Reservation r = new Reservation();
        r.setId(UUID.randomUUID());
        r.setGuestCount(guests);
        r.setReservedAt(reservedAt);
        r.setStatus(ReservationStatus.CONFIRMED);
        return r;
    }

    private TableBriefResponse table(int capacity, TableStatus status) {
        return new TableBriefResponse(UUID.randomUUID(), "B05", TableZone.INDOOR, capacity, status);
    }

    @Test
    void validateTableForReservation_accepts_a_big_enough_usable_table() {
        assertThatCode(() -> validator.validateTableForReservation(reservation(4, NOW), table(6, TableStatus.AVAILABLE)))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validateTableForReservation(reservation(4, NOW), table(4, TableStatus.RESERVED)))
                .doesNotThrowAnyException();
    }

    @Test
    void validateTableForReservation_rejects_missing_small_or_out_of_service_table() {
        assertCode(() -> validator.validateTableForReservation(reservation(4, NOW), null), "VALIDATION_ERROR");
        assertCode(() -> validator.validateTableForReservation(reservation(4, NOW), table(2, TableStatus.AVAILABLE)), "VALIDATION_ERROR");
        assertCode(() -> validator.validateTableForReservation(reservation(4, NOW), table(6, TableStatus.OUT_OF_SERVICE)), "VALIDATION_ERROR");
    }

    @Test
    void validateCustomerCanCancel_needs_at_least_two_hours_left() {
        assertThatCode(() -> validator.validateCustomerCanCancel(reservation(2, NOW.plus(Duration.ofHours(2))))).doesNotThrowAnyException();
        assertCode(() -> validator.validateCustomerCanCancel(reservation(2, NOW.plus(Duration.ofMinutes(119)))), "RESERVATION_CANCEL_TOO_LATE");
    }

    @Test
    void validateStatus_accepts_only_allowed_statuses() {
        Reservation r = reservation(2, NOW);
        assertThatCode(() -> validator.validateStatus(r, Set.of(ReservationStatus.CONFIRMED))).doesNotThrowAnyException();
        assertCode(() -> validator.validateStatus(r, Set.of(ReservationStatus.PENDING)), "RESERVATION_STATUS_INVALID");
    }
}
