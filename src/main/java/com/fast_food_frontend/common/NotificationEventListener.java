package com.fast_food_frontend.common;

import com.fast_food_frontend.dto.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {
    private final JavaMailSender mailSender;

    @Async
    @EventListener
    public void handleOrderEvent(OrderEvent event) {
        log.info("📩 Received event for notification: {}", event);

        String subject;
        String message;

        switch (event.getStatus()) {
            case PENDING -> {
                subject = "Đơn hàng mới #" + event.getOrderId();
                message = "Cảm ơn bạn đã đặt hàng! Mã đơn: " + event.getOrderId() +
                        "\nTrạng thái hiện tại: " + event.getStatus() + "\nThông tin đơn hàng: " + event.getMetadata();
            }
            case PREPARING -> {
                subject = "Đơn hàng #" + event.getOrderId() + " đã được nhà hàng xác nhận";
                message = "Nhà hàng đang chuẩn bị món ăn của bạn!";
            }
            case SHIPPING -> {
                subject = "Đơn hàng #" + event.getOrderId() + " đang được giao";
                message = "Đơn hàng của bạn đang trên đường tới nơi!";
            }
            case DELIVERED -> {
                subject = "Đơn hàng #" + event.getOrderId() + " đã được giao thành công";
                message = "Cảm ơn bạn đã sử dụng dịch vụ! Hẹn gặp lại!";
            }
            case CANCELLED -> {
                subject = "Đơn hàng #" + event.getOrderId() + " đã bị hủy";
                message = "Rất tiếc, đơn hàng của bạn đã bị hủy. Vui lòng thử lại hoặc liên hệ hỗ trợ.";
            }
            default -> {
                subject = "Cập nhật đơn hàng #" + event.getOrderId();
                message = "Trạng thái đơn hàng hiện tại: " + event.getStatus();
            }
        }

        sendEmail(event.getEmailTo(), subject, message);
    }

    private void sendEmail(
            List<String> toList,
            String subject,
            String text
    ) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();

            if (toList != null && !toList.isEmpty()) {
                mail.setTo(toList.toArray(new String[0]));
            }

            mail.setSubject(subject);
            mail.setText(text);
            mailSender.send(mail);

            log.info("Email sent to: {} ", toList);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toList, e.getMessage(), e);
        }
    }

}
