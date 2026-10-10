package com.restaurant.modules.table.service;

import com.restaurant.common.response.PageRequestParams;
import com.restaurant.modules.table.dto.request.TableCreateRequest;
import com.restaurant.modules.table.dto.request.TableSearchRequest;
import com.restaurant.modules.table.dto.request.TableUpdateRequest;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.dto.response.TableResponse;
import com.restaurant.modules.table.enums.TableStatus;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Bàn của nhà hàng: tìm, xem sơ đồ và quản lý (UC007, UC011) cùng API trạng thái cho các module đặt bàn, gọi món, thanh toán.
 * Quản lý chỉ đặt tay {@code AVAILABLE}/{@code OUT_OF_SERVICE}; các trạng thái còn lại đổi qua {@link #transitionStatus}.
 */
public interface TableService {

    Page<TableResponse> searchTables(TableSearchRequest request, PageRequestParams params);

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}
     */
    TableResponse getTable(UUID id);

    /**
     * Thêm bàn ở trạng thái {@code AVAILABLE}.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code TABLE_NAME_EXISTS}
     */
    TableResponse createTable(TableCreateRequest request);

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code TABLE_NAME_EXISTS},
     *                                                          {@code TABLE_CAPACITY_BELOW_RESERVATION}, {@code TABLE_STATUS_NOT_EDITABLE},
     *                                                          {@code TABLE_IN_USE}, {@code VALIDATION_ERROR}
     */
    TableResponse updateTable(UUID id, TableUpdateRequest request);

    /**
     * Xóa cứng bàn; mỗi {@link TableDeletionGuard} được hỏi trước.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code TABLE_IN_USE}
     */
    void deleteTable(UUID id);

    /** Bàn chưa tạm khóa có sức chứa tối thiểu cho trước, bàn vừa khít trước; việc loại bàn trùng giờ là của module đặt bàn. */
    List<TableBriefResponse> listBookableTables(int minCapacity);

    /** Thông tin bàn để module khác hiển thị tên; id không tồn tại bị bỏ qua. */
    List<TableBriefResponse> getTableBriefs(Collection<UUID> ids);

    /**
     * Đổi trạng thái bàn chỉ khi bàn đang ở một trong {@code from} (một câu UPDATE nguyên tử).
     *
     * @return {@code true} nếu đã đổi; {@code false} nếu bàn không còn ở trạng thái mong đợi hoặc không tồn tại
     */
    boolean transitionStatus(UUID id, Collection<TableStatus> from, TableStatus to);
}
