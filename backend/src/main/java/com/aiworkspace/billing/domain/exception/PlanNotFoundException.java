package com.aiworkspace.billing.domain.exception;

import com.aiworkspace.shared.exception.NotFoundException;

public class PlanNotFoundException extends NotFoundException {
    public PlanNotFoundException(String name) {
        super("Plan not found: " + name);
    }
}
