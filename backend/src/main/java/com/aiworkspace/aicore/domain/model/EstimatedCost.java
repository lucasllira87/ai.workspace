package com.aiworkspace.aicore.domain.model;

import java.math.BigDecimal;

public record EstimatedCost(BigDecimal value, String currency) {

    public static EstimatedCost of(BigDecimal value) {
        return new EstimatedCost(value, "USD");
    }

    public static EstimatedCost ofUsd(double value) {
        return new EstimatedCost(BigDecimal.valueOf(value), "USD");
    }

    public static EstimatedCost zero() {
        return new EstimatedCost(BigDecimal.ZERO, "USD");
    }

    public EstimatedCost add(EstimatedCost other) {
        return new EstimatedCost(this.value.add(other.value), this.currency);
    }
}
