package com.restaurant.modules.reservation.repository;

import com.restaurant.modules.reservation.entity.Reservation;
import com.restaurant.modules.reservation.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID>, JpaSpecificationExecutor<Reservation> {

    /** Khóa dòng đặt bàn ({@code FOR UPDATE}) trước khi đổi trạng thái; thứ tự khóa là đặt bàn → đơn → bàn. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);

    Optional<Reservation> findByIdAndCustomerId(UUID id, UUID customerId);

    /**
     * Id các bàn đã có đặt bàn xác nhận (hoặc đã nhận bàn) chồng khung giờ {@code [from, to)}. Là truy vấn trung tâm của việc
     * kiểm tra còn bàn, gợi ý khung giờ và danh sách bàn gán được.
     */
    @Query("""
            select distinct r.tableId from Reservation r
            where r.tableId is not null
              and r.status in (com.restaurant.modules.reservation.enums.ReservationStatus.CONFIRMED,
                               com.restaurant.modules.reservation.enums.ReservationStatus.CHECKED_IN)
              and r.reservedAt < :to and r.reservedEnd > :from
            """)
    Set<UUID> findBusyTableIds(@Param("from") Instant from, @Param("to") Instant to);

    /** Đặt bàn đã xác nhận có bàn, giờ hẹn trong khoảng {@code (noShowCutoff, holdUntil]}: tới lúc giữ bàn mà chưa quá hạn không đến. */
    @Query("""
            select r from Reservation r
            where r.status = com.restaurant.modules.reservation.enums.ReservationStatus.CONFIRMED and r.tableId is not null
              and r.reservedAt <= :holdUntil and r.reservedAt > :noShowCutoff
            order by r.reservedAt asc
            """)
    List<Reservation> findDueForHold(@Param("holdUntil") Instant holdUntil, @Param("noShowCutoff") Instant noShowCutoff, Pageable pageable);

    /** Đặt bàn chưa nhận bàn (chờ xác nhận hoặc đã xác nhận) mà giờ hẹn đã quá {@code cutoff}. */
    @Query("""
            select r from Reservation r
            where r.status in (com.restaurant.modules.reservation.enums.ReservationStatus.PENDING,
                               com.restaurant.modules.reservation.enums.ReservationStatus.CONFIRMED)
              and r.reservedAt < :cutoff
            order by r.reservedAt asc
            """)
    List<Reservation> findDueForNoShow(@Param("cutoff") Instant cutoff, Pageable pageable);

    boolean existsByCustomerIdAndStatusIn(UUID customerId, Collection<ReservationStatus> statuses);

    boolean existsByTableIdAndStatusAndReservedEndAfter(UUID tableId, ReservationStatus status, Instant after);

    /** Số khách lớn nhất trong các đặt bàn đã xác nhận chưa hết khung giờ của bàn; 0 nếu không có. */
    @Query("""
            select coalesce(max(r.guestCount), 0) from Reservation r
            where r.tableId = :tableId and r.status = com.restaurant.modules.reservation.enums.ReservationStatus.CONFIRMED
              and r.reservedEnd > :now
            """)
    int maxGuestCountConfirmedForTable(@Param("tableId") UUID tableId, @Param("now") Instant now);
}
