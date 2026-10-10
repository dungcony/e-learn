package com.restaurant.modules.reservation.service.impl;

import com.restaurant.modules.reservation.enums.ReservationStatus;
import com.restaurant.modules.reservation.repository.ReservationRepository;
import com.restaurant.modules.user.service.UserDeletionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Khách hàng còn đặt bàn chờ xác nhận hoặc đã xác nhận thì chưa xóa được (đặt bàn đã hoàn tất thì được). */
@Component
@RequiredArgsConstructor
public class ReservationUserDeletionGuard implements UserDeletionGuard {

    private final ReservationRepository reservationRepository;

    @Override
    public boolean hasBlockingData(UUID userId) {
        return reservationRepository.existsByCustomerIdAndStatusIn(userId, List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));
    }
}
