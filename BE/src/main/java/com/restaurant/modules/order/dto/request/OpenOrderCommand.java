package com.restaurant.modules.order.dto.request;

import java.util.UUID;

/**
 * Lệnh mở đơn nội bộ, gọi từ module đặt bàn lúc nhận bàn hoặc từ chính module này cho khách vãng lai.
 *
 * @param reservationId      đặt bàn sinh ra đơn; {@code null} với khách vãng lai
 * @param customerId         tài khoản khách hàng của đặt bàn; {@code null} nếu không có
 * @param waiterId           người mở đơn, cũng là nhân viên phụ trách bàn
 * @param allowReservedTable {@code true} khi bàn đang giữ chính cho đặt bàn này nên nhận được dù đang {@code RESERVED}
 */
public record OpenOrderCommand(UUID tableId, UUID reservationId, UUID customerId, UUID waiterId, boolean allowReservedTable) {
}
