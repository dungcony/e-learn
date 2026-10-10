package com.restaurant.modules.reservation.service.impl;

import com.restaurant.common.config.RestaurantProperties;
import com.restaurant.modules.reservation.entity.Reservation;
import com.restaurant.modules.reservation.enums.ReservationStatus;
import com.restaurant.modules.reservation.repository.ReservationRepository;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationMaintenanceServiceImplTest {

    // 09/10/2026 19:00 giờ Việt Nam
    private static final Instant NOW = Instant.parse("2026-10-09T12:00:00Z");

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TableService tableService;
    @Mock
    private PlatformTransactionManager transactionManager;

    private ReservationMaintenanceServiceImpl service;
    private final UUID tableId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        RestaurantProperties properties = new RestaurantProperties(LocalTime.of(10, 0), LocalTime.of(22, 0),
                Duration.ofMinutes(120), Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2), 30, 2, 4,
                new BigDecimal("0.08"), 366);
        service = new ReservationMaintenanceServiceImpl(reservationRepository, tableService, properties,
                Clock.fixed(NOW, ZoneId.of("Asia/Ho_Chi_Minh")), new TransactionTemplate(transactionManager));
    }

    private Reservation reservation(ReservationStatus status, Instant reservedAt, UUID table) {
        Reservation r = new Reservation();
        r.setId(UUID.randomUUID());
        r.setStatus(status);
        r.setReservedAt(reservedAt);
        r.setReservedEnd(reservedAt.plus(Duration.ofMinutes(120)));
        r.setTableId(table);
        return r;
    }

    // ---- giữ bàn khi gần giờ ----

    @Test
    void holdUpcomingTables_marks_the_table_reserved_for_a_confirmed_reservation_due_soon() {
        Reservation r = reservation(ReservationStatus.CONFIRMED, NOW.plus(Duration.ofMinutes(50)), tableId);
        when(reservationRepository.findDueForHold(NOW.plus(Duration.ofMinutes(60)), NOW.minus(Duration.ofMinutes(15)), PageRequest.of(0, 200)))
                .thenReturn(List.of(r));
        when(reservationRepository.findById(r.getId())).thenReturn(Optional.of(r));

        int handled = service.holdUpcomingTables();

        assertThat(handled).isEqualTo(1);
        verify(tableService).transitionStatus(tableId, Set.of(TableStatus.AVAILABLE), TableStatus.RESERVED);
    }

    @Test
    void holdUpcomingTables_skips_a_reservation_that_changed_state_in_the_meantime() {
        Reservation due = reservation(ReservationStatus.CONFIRMED, NOW.plus(Duration.ofMinutes(50)), tableId);
        Reservation reloaded = reservation(ReservationStatus.CANCELLED, NOW.plus(Duration.ofMinutes(50)), tableId);
        reloaded.setId(due.getId());
        when(reservationRepository.findDueForHold(any(), any(), any())).thenReturn(List.of(due));
        when(reservationRepository.findById(due.getId())).thenReturn(Optional.of(reloaded));

        assertThat(service.holdUpcomingTables()).isZero();
        verify(tableService, never()).transitionStatus(any(), any(), any());
    }

    @Test
    void holdUpcomingTables_with_nothing_due_does_nothing() {
        when(reservationRepository.findDueForHold(any(), any(), any())).thenReturn(List.of());

        assertThat(service.holdUpcomingTables()).isZero();
        verify(tableService, never()).transitionStatus(any(), any(), any());
    }

    @Test
    void holdUpcomingTables_keeps_going_when_one_reservation_fails() {
        Reservation bad = reservation(ReservationStatus.CONFIRMED, NOW.plus(Duration.ofMinutes(50)), UUID.randomUUID());
        Reservation good = reservation(ReservationStatus.CONFIRMED, NOW.plus(Duration.ofMinutes(55)), tableId);
        when(reservationRepository.findDueForHold(any(), any(), any())).thenReturn(List.of(bad, good));
        when(reservationRepository.findById(bad.getId())).thenThrow(new IllegalStateException("db down"));
        when(reservationRepository.findById(good.getId())).thenReturn(Optional.of(good));

        assertThat(service.holdUpcomingTables()).isEqualTo(1);
        verify(tableService).transitionStatus(tableId, Set.of(TableStatus.AVAILABLE), TableStatus.RESERVED);
    }

    // ---- không đến ----

    @Test
    void markNoShows_flags_a_confirmed_reservation_and_frees_its_held_table() {
        Reservation r = reservation(ReservationStatus.CONFIRMED, NOW.minus(Duration.ofMinutes(16)), tableId);
        when(reservationRepository.findDueForNoShow(NOW.minus(Duration.ofMinutes(15)), PageRequest.of(0, 200))).thenReturn(List.of(r));
        when(reservationRepository.findByIdForUpdate(r.getId())).thenReturn(Optional.of(r));

        int handled = service.markNoShows();

        assertThat(handled).isEqualTo(1);
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
        verify(tableService).transitionStatus(tableId, Set.of(TableStatus.RESERVED), TableStatus.AVAILABLE);
    }

    @Test
    void markNoShows_flags_a_pending_reservation_without_touching_any_table() {
        Reservation r = reservation(ReservationStatus.PENDING, NOW.minus(Duration.ofMinutes(16)), null);
        when(reservationRepository.findDueForNoShow(any(), any())).thenReturn(List.of(r));
        when(reservationRepository.findByIdForUpdate(r.getId())).thenReturn(Optional.of(r));

        assertThat(service.markNoShows()).isEqualTo(1);
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
        verify(tableService, never()).transitionStatus(any(), any(), any());
    }

    @Test
    void markNoShows_leaves_a_reservation_checked_in_meanwhile() {
        Reservation due = reservation(ReservationStatus.CONFIRMED, NOW.minus(Duration.ofMinutes(16)), tableId);
        Reservation reloaded = reservation(ReservationStatus.CHECKED_IN, NOW.minus(Duration.ofMinutes(16)), tableId);
        reloaded.setId(due.getId());
        when(reservationRepository.findDueForNoShow(any(), any())).thenReturn(List.of(due));
        when(reservationRepository.findByIdForUpdate(due.getId())).thenReturn(Optional.of(reloaded));

        assertThat(service.markNoShows()).isZero();
        assertThat(reloaded.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        verify(tableService, never()).transitionStatus(any(), any(), any());
    }
}
