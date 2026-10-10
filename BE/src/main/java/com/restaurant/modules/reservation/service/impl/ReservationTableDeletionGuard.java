package com.restaurant.modules.reservation.service.impl;

import com.restaurant.modules.reservation.enums.ReservationStatus;
import com.restaurant.modules.reservation.repository.ReservationRepository;
import com.restaurant.modules.table.service.TableDeletionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

/** Bàn còn đặt bàn đã xác nhận chưa hết khung giờ thì không xóa hay tạm khóa được, và không giảm sức chứa dưới số khách đã gán. */
@Component
@RequiredArgsConstructor
public class ReservationTableDeletionGuard implements TableDeletionGuard {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    @Override
    public boolean hasBlockingData(UUID tableId) {
        return reservationRepository.existsByTableIdAndStatusAndReservedEndAfter(tableId, ReservationStatus.CONFIRMED, clock.instant());
    }

    @Override
    public int maxGuestCountAssigned(UUID tableId) {
        return reservationRepository.maxGuestCountConfirmedForTable(tableId, clock.instant());
    }
}
