package com.aiworkspace.notifications.application.service;

import com.aiworkspace.notifications.application.command.UpdatePreferenceCommand;
import com.aiworkspace.notifications.application.dto.NotificationPreferenceDto;
import com.aiworkspace.notifications.application.port.in.GetPreferencesUseCase;
import com.aiworkspace.notifications.application.port.in.UpdatePreferencesUseCase;
import com.aiworkspace.notifications.application.port.out.NotificationPreferenceRepository;
import com.aiworkspace.notifications.domain.model.NotificationPreference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PreferenceService implements GetPreferencesUseCase, UpdatePreferencesUseCase {

    private final NotificationPreferenceRepository preferenceRepository;

    public PreferenceService(NotificationPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceDto getForUser(UUID userId) {
        NotificationPreference pref = getOrCreateDefault(userId);
        return NotificationPreferenceDto.from(pref);
    }

    @Override
    public NotificationPreferenceDto update(UpdatePreferenceCommand command) {
        NotificationPreference pref = getOrCreateDefault(command.userId());
        pref.update(command.type(), command.enabledChannels());
        NotificationPreference saved = preferenceRepository.save(pref);
        return NotificationPreferenceDto.from(saved);
    }

    private NotificationPreference getOrCreateDefault(UUID userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> preferenceRepository.save(
                        NotificationPreference.createDefault(UUID.randomUUID(), userId)));
    }
}
