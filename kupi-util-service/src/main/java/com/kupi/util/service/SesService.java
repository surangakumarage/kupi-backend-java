package com.kupi.util.service;

import com.kupi.util.domain.request.MailRequestDto;
import com.kupi.util.exception.UtilServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class SesService {

    private final JavaMailSender mailSender;
    private final String sender;

    public SesService(JavaMailSender mailSender,
                      @Value("${email.sender}") String sender) {
        this.mailSender = mailSender;
        this.sender = sender;
    }

    public void sendEmail(MailRequestDto dto) {

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setFrom(sender);
            helper.setTo(dto.getRecipientAddress());
            helper.setSubject(dto.getTitle());
            helper.setText(dto.getContent(), dto.isHtmlBody());
            mailSender.send(mime);
            log.debug("Email sent to {}", dto.getRecipientAddress());
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}", dto != null ? dto.getRecipientAddress() : "unknown", e);
            throw new UtilServiceException("Error occurred while sending email, please try again later", e);
        }
    }
}
