package com.aiworkspace.notifications.application.port.in;

import com.aiworkspace.notifications.application.command.MarkAsReadCommand;

public interface MarkAsReadUseCase {
    void markAsRead(MarkAsReadCommand command);
}
