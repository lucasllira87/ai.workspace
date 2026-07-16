package com.aiworkspace.iam.infrastructure.persistence.adapter;

import com.aiworkspace.iam.domain.entity.RefreshToken;
import com.aiworkspace.iam.domain.repository.RefreshTokenRepository;
import com.aiworkspace.iam.domain.valueobject.UserId;
import com.aiworkspace.iam.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.aiworkspace.iam.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpaEntity entity = toEntity(token);
        jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    public List<RefreshToken> findActiveByUserId(UserId userId) {
        return jpaRepository.findActiveByUserId(userId.value(), Instant.now())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void revokeAllByUserId(UserId userId) {
        jpaRepository.revokeAllByUserId(userId.value(), Instant.now());
    }

    @Override
    public void deleteExpiredTokens() {
        jpaRepository.deleteByExpiresAtBefore(Instant.now());
    }

    private RefreshTokenJpaEntity toEntity(RefreshToken token) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(token.getId());
        entity.setToken(token.getToken());
        entity.setUserId(token.getUserId().value());
        entity.setDeviceName(token.getDeviceName());
        entity.setDeviceType(token.getDeviceType());
        entity.setIpAddress(token.getIpAddress());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevokedAt(token.getRevokedAt());
        entity.setLastUsedAt(token.getLastUsedAt());
        entity.setCreatedAt(token.getCreatedAt());
        return entity;
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId(),
                entity.getToken(),
                new UserId(entity.getUserId()),
                entity.getDeviceName(),
                entity.getDeviceType(),
                entity.getIpAddress(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getLastUsedAt(),
                entity.getCreatedAt()
        );
    }
}
