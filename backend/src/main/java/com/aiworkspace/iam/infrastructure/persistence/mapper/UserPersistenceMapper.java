package com.aiworkspace.iam.infrastructure.persistence.mapper;

import com.aiworkspace.iam.domain.entity.User;
import com.aiworkspace.iam.domain.entity.UserProfile;
import com.aiworkspace.iam.domain.valueobject.Email;
import com.aiworkspace.iam.domain.valueobject.FullName;
import com.aiworkspace.iam.domain.valueobject.HashedPassword;
import com.aiworkspace.iam.domain.valueobject.Role;
import com.aiworkspace.iam.domain.valueobject.UserId;
import com.aiworkspace.iam.domain.valueobject.UserStatus;
import com.aiworkspace.iam.infrastructure.persistence.entity.UserJpaEntity;
import com.aiworkspace.iam.infrastructure.persistence.entity.UserProfileJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserPersistenceMapper {

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId().value());
        entity.setEmail(user.getEmail().value());
        entity.setPasswordHash(user.getPassword().value());
        entity.setFullName(user.getFullName().value());
        entity.setStatus(user.getStatus().name());
        entity.setRoles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());

        if (user.getProfile() != null) {
            UserProfileJpaEntity profileEntity = toProfileEntity(user.getProfile(), entity);
            entity.setProfile(profileEntity);
        }

        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        UserProfile profile = null;
        if (entity.getProfile() != null) {
            profile = toProfileDomain(entity.getProfile());
        }

        Set<Role> roles = entity.getRoles().stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        return User.reconstitute(
                new UserId(entity.getId()),
                new Email(entity.getEmail()),
                HashedPassword.ofHash(entity.getPasswordHash()),
                new FullName(entity.getFullName()),
                UserStatus.valueOf(entity.getStatus()),
                roles,
                profile,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private UserProfileJpaEntity toProfileEntity(UserProfile profile, UserJpaEntity userEntity) {
        UserProfileJpaEntity entity = new UserProfileJpaEntity();
        entity.setId(profile.getProfileId());  // reuse existing ID to prevent constraint violation on update
        entity.setUser(userEntity);
        entity.setBio(profile.getBio());
        entity.setAvatarUrl(profile.getAvatarUrl());
        entity.setPreferredAiProvider(profile.getPreferredAiProvider());
        entity.setPreferredLanguage(profile.getPreferredLanguage());
        entity.setUpdatedAt(profile.getUpdatedAt());
        return entity;
    }

    private UserProfile toProfileDomain(UserProfileJpaEntity entity) {
        return UserProfile.reconstitute(
                entity.getId(),
                entity.getBio(),
                entity.getAvatarUrl(),
                entity.getPreferredAiProvider(),
                entity.getPreferredLanguage(),
                entity.getUpdatedAt()
        );
    }
}
