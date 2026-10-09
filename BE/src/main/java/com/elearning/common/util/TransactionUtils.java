package com.elearning.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Chạy tác vụ ngoài DB (gửi email, ghi Redis) sau khi transaction commit thành công, để rollback không để lại
 * email hay khóa ma, và không giữ kết nối DB trong lúc gọi dịch vụ ngoài.
 */
@Slf4j
public final class TransactionUtils {

    private TransactionUtils() {
    }

    /**
     * Chạy {@code action} sau commit; không có transaction đang chạy thì chạy ngay. Lỗi của {@code action} chỉ
     * được log vì dữ liệu đã commit, ném lỗi lúc này sẽ báo thất bại cho một thao tác đã thành công.
     */
    public static void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runSafely(action);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runSafely(action);
            }
        });
    }

    private static void runSafely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            log.error("Tác vụ sau commit thất bại", ex);
        }
    }
}
