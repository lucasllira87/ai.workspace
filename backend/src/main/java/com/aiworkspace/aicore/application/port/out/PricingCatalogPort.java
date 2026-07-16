package com.aiworkspace.aicore.application.port.out;

import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.aicore.domain.model.ModelId;

public interface PricingCatalogPort {

    EstimatedCost estimateCost(ModelId model, int inputTokens, int outputTokens);
}
