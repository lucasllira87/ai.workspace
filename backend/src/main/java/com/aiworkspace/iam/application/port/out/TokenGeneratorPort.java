package com.aiworkspace.iam.application.port.out;

import com.aiworkspace.iam.domain.entity.User;

public interface TokenGeneratorPort {
    String generateAccessToken(User user);
    String generateRefreshToken();
    String extractUserIdFromToken(String token);
    boolean isTokenValid(String token);
}
