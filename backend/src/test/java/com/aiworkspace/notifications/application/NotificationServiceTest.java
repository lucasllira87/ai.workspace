package com.aiworkspace.notifications.application;

import com.aiworkspace.notifications.application.command.MarkAsReadCommand;
import com.aiworkspace.notifications.application.command.SendNotificationCommand;
import com.aiworkspace.notifications.application.port.out.EmailSenderPort;
import com.aiworkspace.notifications.application.port.out.NotificationPreferenceRepository;
import com.aiworkspace.notifications.application.port.out.NotificationRepository;
import com.aiworkspace.notifications.application.port.out.NotificationUserContextPort;
import com.aiworkspace.notifications.application.port.out.TemplateRendererPort;
import com.aiworkspace.notifications.application.service.NotificationService;
import com.aiworkspace.notifications.domain.exception.NotificationNotFoundException;
import com.aiworkspace.notifications.domain.model.Notification;
import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationPreference;
import com.aiworkspace.notifications.domain.model.NotificationType;
import com.aiworkspace.notifications.domain.model.RenderedNotification;
import com.aiworkspace.shared.exception.DomainException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock NotificationPreferenceRepository preferenceRepository;
    @Mock TemplateRendererPort templateRenderer;
    @Mock EmailSenderPort emailSender;
    @Mock NotificationUserContextPort userContext;

    SimpleMeterRegistry meterRegistry;
    NotificationService service;

    final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new NotificationService(notificationRepository, preferenceRepository,
                templateRenderer, emailSender, userContext, meterRegistry);
    }

    @Test
    void send_email_rendersTemplateAndSendsEmailAndSavesNotification() {
        SendNotificationCommand command = new SendNotificationCommand(
                userId, NotificationType.PAYMENT_FAILED, NotificationChannel.EMAIL,
                Map.of("amount", "99.00"));
        RenderedNotification rendered = new RenderedNotification("Payment Failed", "<html>", "text");

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(templateRenderer.render(any(), any())).thenReturn(rendered);
        when(userContext.emailForUser(userId)).thenReturn("user@example.com");
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.send(command);

        verify(templateRenderer).render(NotificationType.PAYMENT_FAILED, command.variables());
        verify(emailSender).send(eq("user@example.com"), eq(rendered));
        verify(notificationRepository).save(any(Notification.class));
        assertThat(meterRegistry.counter("notifications.sent",
                "type", "PAYMENT_FAILED", "channel", "EMAIL").count()).isEqualTo(1.0);
    }

    @Test
    void send_skipsWhenChannelDisabledInPreferences() {
        NotificationPreference pref = mock(NotificationPreference.class);
        when(pref.isEnabled(any(), any())).thenReturn(false);
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(pref));

        SendNotificationCommand command = new SendNotificationCommand(
                userId, NotificationType.PAYMENT_FAILED, NotificationChannel.EMAIL, Map.of());

        service.send(command);

        verifyNoInteractions(templateRenderer, emailSender);
        verify(notificationRepository, never()).save(any());
        assertThat(meterRegistry.counter("notifications.skipped",
                "type", "PAYMENT_FAILED", "channel", "EMAIL").count()).isEqualTo(1.0);
    }

    @Test
    void send_marksFailedAndSavesWhenEmailSenderThrows() {
        SendNotificationCommand command = new SendNotificationCommand(
                userId, NotificationType.PAYMENT_FAILED, NotificationChannel.EMAIL, Map.of());
        RenderedNotification rendered = new RenderedNotification("Subject", "<html>", "text");

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(templateRenderer.render(any(), any())).thenReturn(rendered);
        when(userContext.emailForUser(userId)).thenReturn("user@example.com");
        doThrow(new RuntimeException("SMTP connection refused")).when(emailSender).send(any(), any());
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.send(command);

        verify(notificationRepository).save(any(Notification.class));
        assertThat(meterRegistry.counter("notifications.failed",
                "type", "PAYMENT_FAILED", "channel", "EMAIL").count()).isEqualTo(1.0);
    }

    @Test
    void send_inApp_doesNotCallEmailSender() {
        SendNotificationCommand command = new SendNotificationCommand(
                userId, NotificationType.DOCUMENT_INDEXED, NotificationChannel.IN_APP, Map.of());
        RenderedNotification rendered = new RenderedNotification("Subject", "<html>", "text");

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(templateRenderer.render(any(), any())).thenReturn(rendered);
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.send(command);

        verifyNoInteractions(emailSender);
        verify(notificationRepository).save(any());
    }

    @Test
    void markAsRead_updatesNotificationAndIncrementsCounter() {
        UUID notifId = UUID.randomUUID();
        Notification notification = Notification.create(notifId, userId,
                NotificationType.DOCUMENT_INDEXED, NotificationChannel.IN_APP,
                new RenderedNotification("Title", "<html>", "text"));
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.markAsRead(new MarkAsReadCommand(notifId, userId));

        verify(notificationRepository).save(notification);
        assertThat(meterRegistry.counter("notifications.read",
                "type", "DOCUMENT_INDEXED").count()).isEqualTo(1.0);
    }

    @Test
    void markAsRead_throwsWhenNotificationBelongsToDifferentUser() {
        UUID notifId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Notification notification = Notification.create(notifId, otherUserId,
                NotificationType.DOCUMENT_INDEXED, NotificationChannel.IN_APP,
                new RenderedNotification("Title", "<html>", "text"));
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.markAsRead(new MarkAsReadCommand(notifId, userId)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void markAsRead_throwsWhenNotificationNotFound() {
        UUID notifId = UUID.randomUUID();
        when(notificationRepository.findById(notifId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(new MarkAsReadCommand(notifId, userId)))
                .isInstanceOf(NotificationNotFoundException.class);
    }
}
