package com.musicstore.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@musicstore.com}")
    private String fromAddress;

    @Value("${app.mail.bcc:}")
    private String bccAddress;

    /**
     * Sends an HTML email to the specified recipient.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param html    HTML body content
     * @return true if sent successfully, false otherwise
     */
    public boolean sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            if (bccAddress != null && !bccAddress.isBlank()) {
                helper.setBcc(bccAddress);
            }

            mailSender.send(message);
            log.info("Email sent successfully to: {}, subject: {}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to: {}, subject: {}, error: {}", to, subject, e.getMessage(), e);
            return false;
        }
    }
}
