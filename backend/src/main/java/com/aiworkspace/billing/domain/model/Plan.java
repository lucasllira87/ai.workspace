package com.aiworkspace.billing.domain.model;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class Plan {

    private final UUID id;
    private final String name;
    private final Money price;
    private final BillingCycle billingCycle;
    private final PlanQuota quota;
    private final Set<PlanFeature> features;
    private final boolean active;

    private Plan(UUID id, String name, Money price, BillingCycle billingCycle,
                 PlanQuota quota, Set<PlanFeature> features, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.billingCycle = billingCycle;
        this.quota = quota;
        this.features = Set.copyOf(features);
        this.active = active;
    }

    public static Plan reconstitute(UUID id, String name, Money price, BillingCycle billingCycle,
                                     PlanQuota quota, Set<PlanFeature> features, boolean active) {
        return new Plan(id, name, price, billingCycle, quota, features, active);
    }

    public boolean hasFeature(PlanFeature feature) {
        return features.contains(feature);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Money getPrice() { return price; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public PlanQuota getQuota() { return quota; }
    public Set<PlanFeature> getFeatures() { return Collections.unmodifiableSet(features); }
    public boolean isActive() { return active; }
}
