package com.aiworkspace.notifications.application.port.out;

import com.aiworkspace.notifications.domain.model.RenderedNotification;

public interface EmailSenderPort {
    void send(String toEmail, RenderedNotification rendered);
}
