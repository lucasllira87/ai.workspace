package com.aiworkspace.iam.application.port.out;

/**
 * Provides token expiration policies to the application layer.
 * Keeps application services decoupled from infrastructure configuration.
 */
public interface TokenPolicyPort {
    long accessTokenExpirationSeconds();
    long refreshTokenExpirationSeconds();
}
