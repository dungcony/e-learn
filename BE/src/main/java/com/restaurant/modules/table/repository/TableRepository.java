package com.restaurant.modules.table.repository;

import com.restaurant.modules.table.entity.DiningTable;
import com.restaurant.modules.table.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TableRepository extends JpaRepository<DiningTable, UUID>, JpaSpecificationExecutor<DiningTable> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    /** Bàn chưa tạm khóa có sức chứa tối thiểu cho trước, bàn vừa khít xếp trước. */
    List<DiningTable> findByStatusNotAndCapacityGreaterThanEqualOrderByCapacityAscNameAsc(TableStatus status, int minCapacity);

    /**
     * Đổi trạng thái chỉ khi bàn đang ở một trong {@code from}: một câu lệnh nguyên tử, không đọc rồi ghi, nên hai người cùng
     * mở một bàn thì chỉ một người thành công.
     *
     * @return số dòng đã đổi (1 nếu thành công, 0 nếu bàn không còn ở trạng thái mong đợi)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update DiningTable t set t.status = :to, t.version = t.version + 1 where t.id = :id and t.status in :from")
    int transition(@Param("id") UUID id, @Param("from") Collection<TableStatus> from, @Param("to") TableStatus to);
}
