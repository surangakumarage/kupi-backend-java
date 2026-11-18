package com.kupi.util.service;

import com.kupi.util.domain.request.MailRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link SesService} class
 */
class SesServiceTest {

    private static final String RECIPIENT_ADDRESS = "recipient@kupi.lk";
    private static final String TITLE = "Mail title";
    private static final String CONTENT = "Mail body";

    @Mock
    private JavaMailSender mailSender;

    private static final String SENDER = "dev@kupi.lk";
    private SesService sesService;


    @BeforeEach
    void setUp() {
        initMocks(this);
        sesService = new SesService(mailSender, SENDER);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void should_SendPlainTextEmail() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        MailRequestDto dto = new MailRequestDto("to@example.com", "Hello", "Body", false);
        sesService.sendEmail(dto);
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage sent = captor.getValue();
        assertThat(sent.getAllRecipients()[0].toString()).isEqualTo("to@example.com");
        assertThat(sent.getSubject()).isEqualTo("Hello");
    }

    @Test
    void should_SendHtmlEmail() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        MailRequestDto dto = new MailRequestDto("to@example.com", "Hi", "<b>HTML</b>", true);
        sesService.sendEmail(dto);
        verify(mailSender).send(any(MimeMessage.class));
    }

}