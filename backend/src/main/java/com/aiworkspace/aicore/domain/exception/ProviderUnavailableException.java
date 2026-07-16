package com.aiworkspace.aicore.domain.exception;

import com.aiworkspace.aicore.domain.model.ProviderId;
import com.aiworkspace.shared.exception.ServiceUnavailableException;

public class ProviderUnavailableException extends ServiceUnavailableException {

    private final ProviderId providerId;

    public ProviderUnavailableException(ProviderId providerId, String reason) {
        super("Provider '" + providerId.value() + "' is unavailable: " + reason);
        this.providerId = providerId;
    }

    public ProviderUnavailableException(ProviderId providerId, String reason, Throwable cause) {
        super("Provider '" + providerId.value() + "' is unavailable: " + reason, cause);
        this.providerId = providerId;
    }

    public ProviderId getProviderId() {
        return providerId;
    }
}
