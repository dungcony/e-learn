package com.restaurant.modules.table.service;

import java.util.UUID;

/**
 * Cổng chặn xóa, tạm khóa hoặc giảm sức chứa bàn do module có dữ liệu liên quan (đặt bàn, đơn hàng) implement; {@code table}
 * không được gọi ngược lên các module đó.
 */
public interface TableDeletionGuard {

    /**
     * @return {@code true} nếu module này còn dữ liệu chưa hoàn tất gắn với bàn: đặt bàn đã xác nhận chưa hết khung giờ,
     *         hoặc đơn hàng đang mở
     */
    boolean hasBlockingData(UUID tableId);

    /**
     * @return số khách lớn nhất trong các đặt bàn đã xác nhận chưa hết khung giờ gán cho bàn; 0 nếu không có
     */
    int maxGuestCountAssigned(UUID tableId);
}
