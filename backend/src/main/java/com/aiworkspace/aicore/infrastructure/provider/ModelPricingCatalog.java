package com.aiworkspace.aicore.infrastructure.provider;

import com.aiworkspace.aicore.application.port.out.PricingCatalogPort;
import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.aicore.domain.model.ModelId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Pricing per 1M tokens in USD. Updated for major models as of 2025.
 * Input and output prices are tracked separately.
 */
@Component
public class ModelPricingCatalog implements PricingCatalogPort {

    private record ModelPrice(double inputPer1M, double outputPer1M) {}

    private static final Map<String, ModelPrice> PRICES = Map.ofEntries(
            // OpenAI
            Map.entry("gpt-4o",             new ModelPrice(5.00, 15.00)),
            Map.entry("gpt-4o-mini",        new ModelPrice(0.15,  0.60)),
            Map.entry("text-embedding-3-small", new ModelPrice(0.02,  0.00)),
            Map.entry("text-embedding-3-large", new ModelPrice(0.13,  0.00)),
            // Anthropic
            Map.entry("claude-opus-4-8",    new ModelPrice(15.00, 75.00)),
            Map.entry("claude-sonnet-4-5",  new ModelPrice(3.00,  15.00)),
            Map.entry("claude-haiku-4-5",   new ModelPrice(0.80,   4.00)),
            // Google
            Map.entry("gemini-2.0-flash",   new ModelPrice(0.075,  0.30)),
            Map.entry("gemini-1.5-pro",     new ModelPrice(1.25,   5.00)),
            Map.entry("text-embedding-004", new ModelPrice(0.00,   0.00))
    );

    public EstimatedCost estimateCost(ModelId model, int inputTokens, int outputTokens) {
        if (model == null) return EstimatedCost.zero();

        ModelPrice price = PRICES.getOrDefault(model.value(), new ModelPrice(0.0, 0.0));
        double cost = (inputTokens / 1_000_000.0 * price.inputPer1M())
                    + (outputTokens / 1_000_000.0 * price.outputPer1M());

        return EstimatedCost.of(BigDecimal.valueOf(cost).setScale(6, RoundingMode.HALF_UP));
    }
}
