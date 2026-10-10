package com.restaurant.modules.billing.repository;

import com.restaurant.modules.billing.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {

    List<InvoiceItem> findByInvoiceIdOrderByPosition(UUID invoiceId);
}
