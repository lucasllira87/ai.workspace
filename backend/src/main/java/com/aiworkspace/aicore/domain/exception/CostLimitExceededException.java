package com.aiworkspace.aicore.domain.exception;

import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.shared.exception.DomainException;

import java.math.BigDecimal;

public class CostLimitExceededException extends DomainException {

    public CostLimitExceededException(EstimatedCost estimated, BigDecimal limit) {
        super(String.format("Request estimated cost $%.6f exceeds configured limit $%.6f",
                estimated.value(), limit));
    }
}
