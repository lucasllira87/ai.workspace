package com.aiworkspace.notifications.infrastructure.template;

import com.aiworkspace.notifications.application.port.out.TemplateRendererPort;
import com.aiworkspace.notifications.domain.model.NotificationType;
import com.aiworkspace.notifications.domain.model.RenderedNotification;
import com.aiworkspace.shared.exception.DomainException;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

@Component
public class ThymeleafTemplateRendererAdapter implements TemplateRendererPort {

    private static final Map<NotificationType, String> TEMPLATE_NAMES = Map.of(
            NotificationType.PAYMENT_FAILED,          "notifications/payment-failed",
            NotificationType.TRIAL_STARTED,           "notifications/trial-started",
            NotificationType.TRIAL_EXPIRING,          "notifications/trial-expiring",
            NotificationType.SUBSCRIPTION_CANCELED,   "notifications/subscription-canceled",
            NotificationType.SUBSCRIPTION_EXPIRED,    "notifications/subscription-expired",
            NotificationType.QUOTA_EXCEEDED,          "notifications/quota-exceeded",
            NotificationType.CERTIFICATE_ISSUED,      "notifications/certificate-issued",
            NotificationType.DOCUMENT_INDEXED,        "notifications/document-indexed",
            NotificationType.DOCUMENT_FAILED,         "notifications/document-failed"
    );

    private static final Map<NotificationType, String> SUBJECTS = Map.of(
            NotificationType.PAYMENT_FAILED,          "Payment failed — action required",
            NotificationType.TRIAL_STARTED,           "Your free trial has started",
            NotificationType.TRIAL_EXPIRING,          "Your trial expires in 3 days",
            NotificationType.SUBSCRIPTION_CANCELED,   "Your subscription has been canceled",
            NotificationType.SUBSCRIPTION_EXPIRED,    "Your subscription has expired",
            NotificationType.QUOTA_EXCEEDED,          "Monthly token quota exceeded",
            NotificationType.CERTIFICATE_ISSUED,      "Certificate issued — congratulations!",
            NotificationType.DOCUMENT_INDEXED,        "Document indexed and ready",
            NotificationType.DOCUMENT_FAILED,         "Document indexing failed"
    );

    private final TemplateEngine templateEngine;

    public ThymeleafTemplateRendererAdapter(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public RenderedNotification render(NotificationType type, Map<String, Object> variables) {
        String templateName = TEMPLATE_NAMES.get(type);
        if (templateName == null) {
            throw new DomainException("No template configured for notification type: " + type);
        }

        Context ctx = new Context(Locale.ENGLISH);
        if (variables != null) {
            variables.forEach(ctx::setVariable);
        }

        String html = templateEngine.process(templateName, ctx);
        String subject = SUBJECTS.getOrDefault(type, type.name());

        return new RenderedNotification(subject, html, null);
    }
}
