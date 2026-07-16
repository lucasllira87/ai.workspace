package com.aiworkspace.iam.infrastructure.config;

import com.aiworkspace.iam.application.port.out.TokenPolicyPort;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenPolicyAdapter implements TokenPolicyPort {

    private final JwtProperties jwtProperties;

    public JwtTokenPolicyAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public long accessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration();
    }

    @Override
    public long refreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshTokenExpiration();
    }
}
