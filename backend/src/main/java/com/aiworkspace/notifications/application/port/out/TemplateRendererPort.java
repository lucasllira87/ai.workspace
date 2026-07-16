package com.aiworkspace.notifications.application.port.out;

import com.aiworkspace.notifications.domain.model.NotificationType;
import com.aiworkspace.notifications.domain.model.RenderedNotification;

import java.util.Map;

public interface TemplateRendererPort {
    RenderedNotification render(NotificationType type, Map<String, Object> variables);
}
