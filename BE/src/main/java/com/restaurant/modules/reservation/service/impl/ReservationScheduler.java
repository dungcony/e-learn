package com.restaurant.modules.reservation.service.impl;

import com.restaurant.modules.reservation.service.ReservationMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Chạy việc nền của đặt bàn mỗi 60 giây (tính từ lúc lần trước kết thúc). Chu kỳ này nên một đặt bàn có thể bị đánh dấu "Không
 * đến" trễ tối đa 1 phút so với mốc 15 phút. Giả định chỉ chạy một instance backend.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationMaintenanceService maintenanceService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void run() {
        try {
            int held = maintenanceService.holdUpcomingTables();
            int noShows = maintenanceService.markNoShows();
            if (held > 0 || noShows > 0) {
                log.info("Việc nền đặt bàn: giữ {} bàn, đánh dấu {} đặt bàn không đến", held, noShows);
            }
        } catch (RuntimeException e) {
            log.error("Việc nền đặt bàn lỗi", e);
        }
    }
}
