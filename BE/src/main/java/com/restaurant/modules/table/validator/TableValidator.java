package com.restaurant.modules.table.validator;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.modules.table.entity.DiningTable;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.repository.TableRepository;
import com.restaurant.modules.table.service.TableDeletionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/** Kiểm tra nghiệp vụ của bàn cần tới CSDL hoặc dữ liệu của module khác (rule 2.6, tầng 2). */
@Component
@RequiredArgsConstructor
public class TableValidator {

    // Quản lý chỉ đặt tay hai trạng thái này; còn lại do hệ thống cập nhật
    private static final Set<TableStatus> MANUAL_STATUSES = Set.of(TableStatus.AVAILABLE, TableStatus.OUT_OF_SERVICE);

    private final TableRepository tableRepository;
    private final ObjectProvider<TableDeletionGuard> deletionGuards;

    /**
     * @param excludeTableId bàn đang sửa (bỏ qua chính nó); {@code null} khi thêm mới
     * @throws BusinessException {@code TABLE_NAME_EXISTS} nếu trùng tên, không phân biệt hoa thường
     */
    public void validateNameUnique(String name, UUID excludeTableId) {
        boolean exists = excludeTableId == null
                ? tableRepository.existsByNameIgnoreCase(name)
                : tableRepository.existsByNameIgnoreCaseAndIdNot(name, excludeTableId);
        if (exists) {
            throw new BusinessException(ErrorCode.TABLE_NAME_EXISTS);
        }
    }

    /**
     * Quy tắc khi Quản lý đặt tay trạng thái bàn.
     *
     * @param requested trạng thái yêu cầu; {@code null} nghĩa là không đổi
     * @throws BusinessException {@code VALIDATION_ERROR} nếu yêu cầu trạng thái do hệ thống quản lý,
     *                           {@code TABLE_STATUS_NOT_EDITABLE} nếu bàn đang được giữ hoặc đang phục vụ,
     *                           {@code TABLE_IN_USE} nếu tạm khóa bàn còn đặt bàn hoặc đơn chưa hoàn tất
     */
    public void validateStatusChange(DiningTable table, TableStatus requested) {
        if (requested == null || requested == table.getStatus()) {
            return;
        }
        if (!MANUAL_STATUSES.contains(requested)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Quản lý chỉ đặt được trạng thái Trống hoặc Tạm khóa.");
        }
        if (table.getStatus() == TableStatus.RESERVED || table.getStatus() == TableStatus.OCCUPIED) {
            throw new BusinessException(ErrorCode.TABLE_STATUS_NOT_EDITABLE);
        }
        if (requested == TableStatus.OUT_OF_SERVICE && hasBlockingData(table.getId())) {
            throw new BusinessException(ErrorCode.TABLE_IN_USE);
        }
    }

    /**
     * @throws BusinessException {@code TABLE_CAPACITY_BELOW_RESERVATION} nếu sức chứa mới nhỏ hơn số khách của đặt bàn đang gán
     */
    public void validateCapacity(UUID tableId, int newCapacity) {
        int maxAssigned = deletionGuards.orderedStream().mapToInt(guard -> guard.maxGuestCountAssigned(tableId)).max().orElse(0);
        if (newCapacity < maxAssigned) {
            throw new BusinessException(ErrorCode.TABLE_CAPACITY_BELOW_RESERVATION);
        }
    }

    /**
     * @throws BusinessException {@code TABLE_IN_USE} nếu bàn đang phục vụ hoặc còn đặt bàn chưa hoàn tất
     */
    public void validateDeletable(DiningTable table) {
        if (table.getStatus() == TableStatus.OCCUPIED || hasBlockingData(table.getId())) {
            throw new BusinessException(ErrorCode.TABLE_IN_USE);
        }
    }

    private boolean hasBlockingData(UUID tableId) {
        return deletionGuards.orderedStream().anyMatch(guard -> guard.hasBlockingData(tableId));
    }
}
