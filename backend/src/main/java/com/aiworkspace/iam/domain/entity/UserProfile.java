package com.aiworkspace.iam.domain.entity;

import java.time.Instant;

public class UserProfile {

    private java.util.UUID profileId;  // opaque persistence ID — not a domain concept
    private String bio;
    private String avatarUrl;
    private String preferredAiProvider;
    private String preferredLanguage;
    private Instant updatedAt;

    private UserProfile() {}

    public static UserProfile createDefault() {
        UserProfile profile = new UserProfile();
        profile.profileId = java.util.UUID.randomUUID();
        profile.preferredLanguage = "pt-BR";
        profile.updatedAt = Instant.now();
        return profile;
    }

    public static UserProfile reconstitute(java.util.UUID profileId, String bio, String avatarUrl,
                                            String preferredAiProvider, String preferredLanguage,
                                            Instant updatedAt) {
        UserProfile profile = new UserProfile();
        profile.profileId = profileId;
        profile.bio = bio;
        profile.avatarUrl = avatarUrl;
        profile.preferredAiProvider = preferredAiProvider;
        profile.preferredLanguage = preferredLanguage != null ? preferredLanguage : "pt-BR";
        profile.updatedAt = updatedAt;
        return profile;
    }

    public void update(String bio, String avatarUrl, String preferredAiProvider, String preferredLanguage) {
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.preferredAiProvider = preferredAiProvider;
        if (preferredLanguage != null && !preferredLanguage.isBlank()) {
            this.preferredLanguage = preferredLanguage;
        }
        this.updatedAt = Instant.now();
    }

    public java.util.UUID getProfileId() { return profileId; }
    public String getBio() { return bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPreferredAiProvider() { return preferredAiProvider; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public Instant getUpdatedAt() { return updatedAt; }
}
