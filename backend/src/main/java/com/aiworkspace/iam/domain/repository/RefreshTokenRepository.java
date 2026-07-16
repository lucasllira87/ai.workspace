package com.aiworkspace.iam.domain.repository;

import com.aiworkspace.iam.domain.entity.RefreshToken;
import com.aiworkspace.iam.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findActiveByUserId(UserId userId);
    void revokeAllByUserId(UserId userId);
    void deleteExpiredTokens();
}
