package com.aiworkspace.iam.domain.entity;

import com.aiworkspace.iam.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    private UUID id;
    private String token;
    private UserId userId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant lastUsedAt;
    private Instant createdAt;

    private RefreshToken() {}

    public static RefreshToken create(UserId userId, String token, String deviceName,
                                       String deviceType, String ipAddress, long expirationSeconds) {
        RefreshToken rt = new RefreshToken();
        rt.id = UUID.randomUUID();
        rt.token = token;
        rt.userId = userId;
        rt.deviceName = deviceName;
        rt.deviceType = deviceType;
        rt.ipAddress = ipAddress;
        rt.expiresAt = Instant.now().plusSeconds(expirationSeconds);
        rt.lastUsedAt = Instant.now();
        rt.createdAt = Instant.now();
        return rt;
    }

    public static RefreshToken reconstitute(UUID id, String token, UserId userId,
                                             String deviceName, String deviceType, String ipAddress,
                                             Instant expiresAt, Instant revokedAt,
                                             Instant lastUsedAt, Instant createdAt) {
        RefreshToken rt = new RefreshToken();
        rt.id = id;
        rt.token = token;
        rt.userId = userId;
        rt.deviceName = deviceName;
        rt.deviceType = deviceType;
        rt.ipAddress = ipAddress;
        rt.expiresAt = expiresAt;
        rt.revokedAt = revokedAt;
        rt.lastUsedAt = lastUsedAt;
        rt.createdAt = createdAt;
        return rt;
    }

    public boolean isValid() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }

    public RefreshToken rotate(String newRawToken, long expirationSeconds) {
        this.revoke();
        return RefreshToken.create(this.userId, newRawToken, this.deviceName,
                this.deviceType, this.ipAddress, expirationSeconds);
    }

    public void recordUsage() {
        this.lastUsedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getToken() { return token; }
    public UserId getUserId() { return userId; }
    public String getDeviceName() { return deviceName; }
    public String getDeviceType() { return deviceType; }
    public String getIpAddress() { return ipAddress; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
