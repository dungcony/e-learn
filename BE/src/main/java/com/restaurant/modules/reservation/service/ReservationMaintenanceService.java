package com.restaurant.modules.reservation.service;

/**
 * Việc nền của đặt bàn, chạy định kỳ bởi {@code ReservationScheduler}. Mỗi đặt bàn được xử lý trong transaction riêng nên một bản
 * ghi lỗi không làm hỏng các bản ghi còn lại.
 */
public interface ReservationMaintenanceService {

    /**
     * Đặt bàn đã xác nhận mà còn không quá 60 phút tới giờ hẹn thì chuyển bàn đã gán sang "Đã đặt" (nếu bàn đang trống).
     *
     * @return số đặt bàn đã xử lý
     */
    int holdUpcomingTables();

    /**
     * Đặt bàn chưa nhận bàn mà quá giờ hẹn 15 phút thì chuyển sang "Không đến" và trả bàn đang giữ về trống.
     *
     * @return số đặt bàn đã chuyển sang không đến
     */
    int markNoShows();
}
