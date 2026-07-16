package com.aiworkspace.billing.application.dto;

import com.aiworkspace.billing.domain.model.BillingCycle;
import com.aiworkspace.billing.domain.model.Money;
import com.aiworkspace.billing.domain.model.Plan;
import com.aiworkspace.billing.domain.model.PlanFeature;
import com.aiworkspace.billing.domain.model.PlanQuota;

import java.util.Set;
import java.util.UUID;

public record PlanDto(UUID id, String name, Money price, BillingCycle billingCycle,
                       PlanQuota quota, Set<PlanFeature> features) {
    public static PlanDto from(Plan plan) {
        return new PlanDto(plan.getId(), plan.getName(), plan.getPrice(),
                plan.getBillingCycle(), plan.getQuota(), plan.getFeatures());
    }
}
