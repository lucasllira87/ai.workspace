package com.aiworkspace.notifications.domain.model;

import java.util.Objects;

public record RenderedNotification(String subject, String bodyHtml, String bodyText) {

    public RenderedNotification {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(bodyHtml, "bodyHtml");
    }
}
