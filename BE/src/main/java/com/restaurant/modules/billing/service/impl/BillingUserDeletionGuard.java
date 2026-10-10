package com.restaurant.modules.billing.service.impl;

import com.restaurant.modules.billing.repository.InvoiceRepository;
import com.restaurant.modules.user.service.UserDeletionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Thu ngân đã lập hóa đơn thì chưa xóa được tài khoản (chỉ khóa), để hóa đơn cũ còn truy ra người lập. */
@Component
@RequiredArgsConstructor
public class BillingUserDeletionGuard implements UserDeletionGuard {

    private final InvoiceRepository invoiceRepository;

    @Override
    public boolean hasBlockingData(UUID userId) {
        return invoiceRepository.existsByCashierId(userId);
    }
}
