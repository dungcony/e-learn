package com.restaurant.modules.reservation.entity;

import com.restaurant.common.abstracts.AssignedIdEntity;
import com.restaurant.modules.reservation.enums.ReservationStatus;
import com.restaurant.modules.table.enums.TableZone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Đặt bàn (bảng {@code reservations}). Khung giờ giữ bàn là {@code [reservedAt, reservedEnd)}; ràng buộc loại trừ của CSDL
 * chặn hai đặt bàn đã xác nhận chồng khung giờ trên cùng một bàn. Khách hàng, bàn và nhân viên là id thuần của module khác.
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
public class Reservation extends AssignedIdEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String code;

    // Có khi khách đặt lúc đã đăng nhập; null với khách vãng lai
    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String phone;

    private String email;

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt;

    @Column(name = "reserved_end", nullable = false)
    private Instant reservedEnd;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_zone")
    private TableZone preferredZone;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    // Bàn được gán lúc xác nhận; null khi còn chờ xác nhận
    @Column(name = "table_id")
    private UUID tableId;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "confirmed_by")
    private UUID confirmedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
