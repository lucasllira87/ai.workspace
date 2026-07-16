package com.aiworkspace.notifications.application.port.in;

import com.aiworkspace.notifications.application.command.UpdatePreferenceCommand;
import com.aiworkspace.notifications.application.dto.NotificationPreferenceDto;

public interface UpdatePreferencesUseCase {
    NotificationPreferenceDto update(UpdatePreferenceCommand command);
}
