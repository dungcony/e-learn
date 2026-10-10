package com.restaurant.modules.billing.repository;

import com.restaurant.modules.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    /** Quyền sở hữu của khách hàng: hóa đơn người khác trả về rỗng như không tồn tại. */
    Optional<Invoice> findByIdAndCustomerId(UUID id, UUID customerId);

    boolean existsByCashierId(UUID cashierId);

    /**
     * Tăng số lần in bằng một câu UPDATE tương đối để hai lần in song song đều được tính; xóa cache cấp một để lần đọc lại
     * thấy số mới.
     *
     * @return số dòng được cập nhật (0 nếu hóa đơn không tồn tại)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Invoice i set i.printCount = i.printCount + 1 where i.id = :id")
    int incrementPrintCount(@Param("id") UUID id);
}
