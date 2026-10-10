package com.restaurant.modules.reservation.service.impl;

import com.restaurant.common.config.RestaurantProperties;
import com.restaurant.modules.reservation.entity.Reservation;
import com.restaurant.modules.reservation.enums.ReservationStatus;
import com.restaurant.modules.reservation.repository.ReservationRepository;
import com.restaurant.modules.reservation.service.ReservationMaintenanceService;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Mỗi đặt bàn chạy trong một transaction riêng bằng {@link TransactionTemplate} (không dùng {@code @Transactional} vì các hàm gọi
 * nhau trong cùng lớp sẽ bỏ qua proxy). Mỗi chu kỳ xử lý tối đa 200 bản ghi; phần còn lại để chu kỳ sau.
 */
@Slf4j
@Service
public class ReservationMaintenanceServiceImpl implements ReservationMaintenanceService {

    private static final Pageable BATCH = PageRequest.of(0, 200);

    private final ReservationRepository reservationRepository;
    private final TableService tableService;
    private final RestaurantProperties properties;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public ReservationMaintenanceServiceImpl(ReservationRepository reservationRepository, TableService tableService,
                                             RestaurantProperties properties, Clock clock, TransactionTemplate transactionTemplate) {
        this.reservationRepository = reservationRepository;
        this.tableService = tableService;
        this.properties = properties;
        this.clock = clock;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public int holdUpcomingTables() {
        Instant now = clock.instant();
        List<Reservation> due = reservationRepository.findDueForHold(
                now.plus(properties.reservationHoldBefore()), now.minus(properties.noShowGrace()), BATCH);
        return processEach(due, "giữ bàn", reservation -> {
            Reservation current = reservationRepository.findById(reservation.getId()).orElse(null);
            if (current == null || current.getStatus() != ReservationStatus.CONFIRMED || current.getTableId() == null) {
                return false;
            }
            // Bàn đang phục vụ hoặc tạm khóa thì không chuyển được; giữ nguyên, chu kỳ sau thử lại
            tableService.transitionStatus(current.getTableId(), Set.of(TableStatus.AVAILABLE), TableStatus.RESERVED);
            return true;
        });
    }

    @Override
    public int markNoShows() {
        Instant cutoff = clock.instant().minus(properties.noShowGrace());
        List<Reservation> due = reservationRepository.findDueForNoShow(cutoff, BATCH);
        return processEach(due, "đánh dấu không đến", reservation -> {
            Reservation current = reservationRepository.findByIdForUpdate(reservation.getId()).orElse(null);
            boolean stillWaiting = current != null
                    && (current.getStatus() == ReservationStatus.PENDING || current.getStatus() == ReservationStatus.CONFIRMED);
            if (!stillWaiting) {
                return false;
            }
            current.setStatus(ReservationStatus.NO_SHOW);
            if (current.getTableId() != null) {
                tableService.transitionStatus(current.getTableId(), Set.of(TableStatus.RESERVED), TableStatus.AVAILABLE);
            }
            return true;
        });
    }

    // Xử lý từng bản ghi trong transaction riêng; lỗi một bản ghi chỉ ghi log rồi đi tiếp
    private int processEach(List<Reservation> reservations, String action, Predicate<Reservation> work) {
        int handled = 0;
        for (Reservation reservation : reservations) {
            try {
                Boolean changed = transactionTemplate.execute(status -> work.test(reservation));
                if (Boolean.TRUE.equals(changed)) {
                    handled++;
                }
            } catch (RuntimeException e) {
                log.error("Lỗi khi {} cho đặt bàn {}", action, reservation.getId(), e);
            }
        }
        return handled;
    }
}
