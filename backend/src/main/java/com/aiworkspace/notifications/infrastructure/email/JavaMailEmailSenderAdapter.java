package com.aiworkspace.notifications.infrastructure.email;

import com.aiworkspace.notifications.application.port.out.EmailSenderPort;
import com.aiworkspace.notifications.domain.model.RenderedNotification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class JavaMailEmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(JavaMailEmailSenderAdapter.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public JavaMailEmailSenderAdapter(JavaMailSender mailSender,
                                       @Value("${notifications.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String toEmail, RenderedNotification rendered) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(rendered.subject());
            helper.setText(rendered.bodyText() != null ? rendered.bodyText() : "", rendered.bodyHtml());
            mailSender.send(message);
            log.debug("Email sent to={} subject={}", toEmail, rendered.subject());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + toEmail, e);
        }
    }
}
