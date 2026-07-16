package com.aiworkspace.notifications.application.port.in;

import com.aiworkspace.notifications.application.command.SendNotificationCommand;

public interface SendNotificationUseCase {
    void send(SendNotificationCommand command);
}
