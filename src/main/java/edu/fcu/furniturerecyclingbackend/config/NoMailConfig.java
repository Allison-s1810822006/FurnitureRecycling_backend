package edu.fcu.furniturerecyclingbackend.config;


//EmailService報錯，為跳過該報錯測試API先寫這個跳過的程式


import org.springframework.context.annotation.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

@Configuration
//@Profile("dev-no-mail")  // 只在這個 profile 生效
public class NoMailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override public MimeMessage createMimeMessage() {
                return new MimeMessage(Session.getDefaultInstance(new Properties()));
            }
            @Override public MimeMessage createMimeMessage(java.io.InputStream contentStream) { return createMimeMessage(); }
            @Override public void send(MimeMessage mimeMessage) { /* no-op */ }
            @Override public void send(MimeMessage... mimeMessages) { /* no-op */ }
            @Override public void send(MimeMessagePreparator mimeMessagePreparator) { /* no-op */ }
            @Override public void send(MimeMessagePreparator... mimeMessagePreparators) { /* no-op */ }
            @Override public void send(SimpleMailMessage simpleMessage) { /* no-op */ }
            @Override public void send(SimpleMailMessage... simpleMessages) { /* no-op */ }
        };
    }
}
