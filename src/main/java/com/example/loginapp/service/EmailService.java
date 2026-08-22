package com.example.loginapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails via Spring Mail. The underlying SMTP session
 * (host / port / username / app-password) is configured entirely through
 * application.properties (spring.mail.*) — this class just builds and
 * hands off the message.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends the one-time password to the given address.
     */
    public void sendOtpEmail(String toAddress, String firstName, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toAddress);
        message.setSubject("Your GlobalTrotter password reset code");
        message.setText(
                "Hi " + (firstName == null || firstName.isBlank() ? "there" : firstName) + ",\n\n" +
                "We received a request to reset your GlobalTrotter password.\n\n" +
                "Your one-time verification code is:\n\n" +
                "    " + otp + "\n\n" +
                "This code expires in " + otpExpiryMinutes + " minutes. If you didn't request a " +
                "password reset, you can safely ignore this email.\n\n" +
                "— GlobalTrotter"
        );
        mailSender.send(message);
    }
}
