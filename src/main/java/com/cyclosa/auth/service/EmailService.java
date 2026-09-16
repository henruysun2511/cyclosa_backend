package com.cyclosa.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.frontend.activation-url:http://localhost:5173/activate}")
    private String activationUrlBase;

    public void sendActivationEmail(String toEmail, String fullName, String activationToken) {
        String activationUrl = activationUrlBase + "?token=" + activationToken;

        log.info("================================================================================");
        log.info("[EMAIL ACTIVATION] Người nhận: {} ({})", fullName, toEmail);
        log.info("[EMAIL ACTIVATION] Token: {}", activationToken);
        log.info("[EMAIL ACTIVATION] Đường dẫn kích hoạt: {}", activationUrl);
        log.info("================================================================================");

        if (mailSender != null && StringUtils.hasText(mailFrom)) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(mailFrom);
                message.setTo(toEmail);
                message.setSubject("[CYCLOSA] Thư mời kích hoạt tài khoản làm việc");
                message.setText(String.format(
                        "Xin chào %s,\n\n" +
                        "Bạn vừa được tạo tài khoản làm việc trên hệ thống quản trị nhân sự CYCLOSA.\n" +
                        "Vui lòng nhấn vào liên kết dưới đây để thiết lập mật khẩu và kích hoạt tài khoản:\n" +
                        "%s\n\n" +
                        "Liên kết có hiệu lực trong vòng 48 giờ.\n" +
                        "Trân trọng,\nĐội ngũ CYCLOSA HRM.",
                        fullName, activationUrl
                ));

                mailSender.send(message);
                log.info("[EMAIL ACTIVATION] Đã gửi thư thành công tới {}", toEmail);
            } catch (Exception e) {
                log.warn("[EMAIL ACTIVATION] Gửi email thật qua SMTP thất bại ({}), dev có thể dùng đường dẫn kích hoạt trong log.", e.getMessage());
            }
        } else {
            log.info("[EMAIL ACTIVATION] SMTP chưa được cấu hình tài khoản gửi (spring.mail.username trống). Bỏ qua gửi email thực tế.");
        }
    }
}
