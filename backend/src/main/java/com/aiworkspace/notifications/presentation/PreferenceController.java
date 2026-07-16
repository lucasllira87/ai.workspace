package com.aiworkspace.notifications.presentation;

import com.aiworkspace.notifications.application.command.UpdatePreferenceCommand;
import com.aiworkspace.notifications.application.dto.NotificationPreferenceDto;
import com.aiworkspace.notifications.application.port.in.GetPreferencesUseCase;
import com.aiworkspace.notifications.application.port.in.UpdatePreferencesUseCase;
import com.aiworkspace.notifications.application.port.out.NotificationUserContextPort;
import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications/preferences")
public class PreferenceController {

    private final GetPreferencesUseCase getPreferences;
    private final UpdatePreferencesUseCase updatePreferences;
    private final NotificationUserContextPort userContext;

    public PreferenceController(GetPreferencesUseCase getPreferences,
                                 UpdatePreferencesUseCase updatePreferences,
                                 NotificationUserContextPort userContext) {
        this.getPreferences = getPreferences;
        this.updatePreferences = updatePreferences;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<NotificationPreferenceDto> get() {
        UUID userId = userContext.currentUserId();
        return ResponseEntity.ok(getPreferences.getForUser(userId));
    }

    @PutMapping
    public ResponseEntity<NotificationPreferenceDto> update(@Valid @RequestBody UpdatePreferenceRequest request) {
        UUID userId = userContext.currentUserId();
        NotificationType type = NotificationType.valueOf(request.type());
        Set<NotificationChannel> channels = request.enabledChannels().stream()
                .map(NotificationChannel::valueOf)
                .collect(Collectors.toSet());
        NotificationPreferenceDto result = updatePreferences.update(
                new UpdatePreferenceCommand(userId, type, channels));
        return ResponseEntity.ok(result);
    }

    record UpdatePreferenceRequest(
            @NotNull String type,
            @NotNull Set<String> enabledChannels) {}
}
