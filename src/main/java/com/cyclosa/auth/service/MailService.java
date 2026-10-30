package com.cyclosa.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.activation-url:http://localhost:5173/auth/activate}")
    private String activationBaseUrl;

    @Value("${spring.mail.username:no-reply@cyclosa.com}")
    private String fromEmail;

    /**
     * Gửi liên kết kích hoạt tài khoản có thời hạn 48 giờ cho nhân viên mới
     */
    @Async
    public void sendActivationEmail(String toEmail, String activationToken, Duration ttl) {
        String activationUrl = activationBaseUrl + "?token=" + activationToken;
        long hours = ttl.toHours();

        log.info("Chuẩn bị gửi email kích hoạt tài khoản tới {} (Token TTL: {} giờ, Link: {})",
                toEmail, hours, activationUrl);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[CYCLOSA HRM] Kích hoạt tài khoản nhân viên mới");
            message.setText("Xin chào,\n\n"
                    + "Tài khoản của bạn trên hệ thống CYCLOSA HRM đã được khởi tạo.\n"
                    + "Vui lòng truy cập đường dẫn sau để đặt mật khẩu lần đầu và kích hoạt tài khoản (liên kết có hiệu lực trong " + hours + " giờ):\n\n"
                    + activationUrl + "\n\n"
                    + "Nếu bạn không phải là người nhận mong muốn, vui lòng bỏ qua thư này.\n"
                    + "Trân trọng,\nĐội ngũ Quản trị Nhân sự CYCLOSA.");

            mailSender.send(message);
            log.info("Đã gửi email kích hoạt thành công tới {}", toEmail);
        } catch (Exception e) {
            log.warn("Không thể gửi email kích hoạt tới {} (Lưu ý: trong môi trường dev nếu chưa cấu hình SMTP, bạn có thể lấy link từ log): {}",
                    toEmail, e.getMessage());
        }
    }

    /**
     * Gửi mã xác thực OTP khôi phục mật khẩu qua email (thời hạn 15 phút)
     */
    @Async
    public void sendPasswordResetOtpEmail(String toEmail, String otp, Duration ttl) {
        long minutes = ttl.toMinutes();

        log.info("Chuẩn bị gửi email OTP khôi phục mật khẩu tới {} (OTP: {}, TTL: {} phút)",
                toEmail, otp, minutes);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[CYCLOSA HRM] Mã OTP xác thực khôi phục mật khẩu");
            message.setText("Xin chào,\n\n"
                    + "Hệ thống CYCLOSA HRM vừa nhận được yêu cầu khôi phục mật khẩu cho tài khoản này.\n\n"
                    + "Mã OTP xác thực của bạn là: " + otp + "\n\n"
                    + "Mã OTP có hiệu lực trong vòng " + minutes + " phút. Tuyệt đối không chia sẻ mã này cho bất kỳ ai.\n"
                    + "Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng liên hệ ngay với Quản trị viên hệ thống.\n\n"
                    + "Trân trọng,\nĐội ngũ Bảo mật CYCLOSA HRM.");

            mailSender.send(message);
            log.info("Đã gửi email OTP khôi phục mật khẩu thành công tới {}", toEmail);
        } catch (Exception e) {
            log.warn("Không thể gửi email OTP tới {} (Lưu ý: trong môi trường dev có thể lấy OTP từ log): {}",
                    toEmail, e.getMessage());
        }
    }
}
