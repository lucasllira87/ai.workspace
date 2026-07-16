package com.aiworkspace.iam.application.mapper;

import com.aiworkspace.iam.application.dto.UserDto;
import com.aiworkspace.iam.domain.entity.User;

import java.util.stream.Collectors;

public final class UserDtoMapper {

    private UserDtoMapper() {}

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId().toString(),
                user.getEmail().value(),
                user.getFullName().value(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.getStatus().name()
        );
    }
}
