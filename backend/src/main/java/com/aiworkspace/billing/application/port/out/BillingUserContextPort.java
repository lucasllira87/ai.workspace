package com.aiworkspace.billing.application.port.out;

import java.util.UUID;

public interface BillingUserContextPort {
    UUID getCurrentUserId();
}
