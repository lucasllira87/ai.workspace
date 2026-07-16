package com.aiworkspace.billing.application.port.in;

import com.aiworkspace.billing.application.command.UpgradeSubscriptionCommand;
import com.aiworkspace.billing.application.dto.SubscriptionDto;

public interface UpgradeSubscriptionUseCase {
    SubscriptionDto upgrade(UpgradeSubscriptionCommand command);
}
