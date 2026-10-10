package com.restaurant.modules.reservation.events;

import com.restaurant.common.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Gửi email thông báo đặt bàn sau khi transaction đã commit, để không báo cho khách một thay đổi bị rollback. Khách không nhập
 * email thì bỏ qua; lỗi gửi chỉ ghi log WARN (không kèm nội dung) và không làm hỏng nghiệp vụ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationNotificationListener {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");

    private final EmailService emailService;
    private final Clock clock;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(ReservationCreatedEvent event) {
        send(event.email(), "Đã nhận yêu cầu đặt bàn " + event.code(), """
                Chào %s,

                Nhà hàng đã nhận yêu cầu đặt bàn %s lúc %s cho %d khách.
                Nhân viên sẽ xác nhận và thông báo kết quả cho bạn qua email này.
                """.formatted(event.guestName(), event.code(), format(event.reservedAt()), event.guestCount()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onConfirmed(ReservationConfirmedEvent event) {
        send(event.email(), "Đặt bàn " + event.code() + " đã được xác nhận", """
                Chào %s,

                Đặt bàn %s của bạn lúc %s đã được xác nhận, bàn %s.
                Vui lòng đến đúng giờ; bàn được giữ tối đa 15 phút sau giờ hẹn.
                """.formatted(event.guestName(), event.code(), format(event.reservedAt()), event.tableName()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCancelled(ReservationCancelledEvent event) {
        send(event.email(), "Đặt bàn " + event.code() + " đã bị hủy", """
                Chào %s,

                Rất tiếc, đặt bàn %s lúc %s đã bị hủy.
                Lý do: %s
                """.formatted(event.guestName(), event.code(), format(event.reservedAt()), event.reason()));
    }

    private void send(String email, String subject, String content) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        try {
            emailService.sendEmail(email, subject, content);
        } catch (RuntimeException e) {
            log.warn("Không gửi được email thông báo đặt bàn", e);
        }
    }

    private String format(Instant time) {
        return TIME_FORMAT.format(time.atZone(clock.getZone()));
    }
}
