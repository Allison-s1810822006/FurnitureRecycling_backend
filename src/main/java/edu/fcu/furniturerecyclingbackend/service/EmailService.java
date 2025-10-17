package edu.fcu.furniturerecyclingbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("註冊郵件驗證");
        message.setText("請點擊以下鏈接進行帳號驗證：\n" + verificationLink);
        mailSender.send(message);
    }
}

