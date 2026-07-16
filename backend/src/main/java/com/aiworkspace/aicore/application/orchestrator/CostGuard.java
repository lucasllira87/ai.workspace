package com.aiworkspace.aicore.application.orchestrator;

import com.aiworkspace.aicore.application.port.out.AIConfigPort;
import com.aiworkspace.aicore.application.port.out.PricingCatalogPort;
import com.aiworkspace.aicore.domain.exception.CostLimitExceededException;
import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.aicore.domain.model.ModelId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CostGuard {

    private final AIConfigPort aiConfig;
    private final PricingCatalogPort pricingCatalog;

    public CostGuard(AIConfigPort aiConfig, PricingCatalogPort pricingCatalog) {
        this.aiConfig = aiConfig;
        this.pricingCatalog = pricingCatalog;
    }

    public void checkEstimatedCost(ModelId model, int estimatedInputTokens) {
        if (!aiConfig.isCostGuardEnabled()) return;

        BigDecimal maxCost = aiConfig.getMaxCostPerRequest();
        // Estimate conservatively: input tokens * 2 to account for output
        EstimatedCost estimated = pricingCatalog.estimateCost(model, estimatedInputTokens,
                estimatedInputTokens);

        if (estimated.value().compareTo(maxCost) > 0) {
            throw new CostLimitExceededException(estimated, maxCost);
        }
    }
}
