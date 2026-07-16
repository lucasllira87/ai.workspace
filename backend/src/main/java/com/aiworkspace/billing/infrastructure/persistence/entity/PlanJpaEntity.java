package com.aiworkspace.billing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plans", schema = "billing")
public class PlanJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private BigDecimal priceAmount;

    @Column(nullable = false, length = 3)
    private String priceCurrency;

    @Column(nullable = false)
    private String billingCycle;

    @Column(nullable = false)
    private long maxTokensPerMonth;

    @Column(nullable = false)
    private int maxDocuments;

    @Column(nullable = false)
    private long maxStorageBytes;

    @Column(nullable = false)
    private int maxEnrollments;

    @Column(columnDefinition = "TEXT[]")
    private String featuresJson;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public void setPriceAmount(BigDecimal priceAmount) { this.priceAmount = priceAmount; }
    public String getPriceCurrency() { return priceCurrency; }
    public void setPriceCurrency(String priceCurrency) { this.priceCurrency = priceCurrency; }
    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }
    public long getMaxTokensPerMonth() { return maxTokensPerMonth; }
    public void setMaxTokensPerMonth(long maxTokensPerMonth) { this.maxTokensPerMonth = maxTokensPerMonth; }
    public int getMaxDocuments() { return maxDocuments; }
    public void setMaxDocuments(int maxDocuments) { this.maxDocuments = maxDocuments; }
    public long getMaxStorageBytes() { return maxStorageBytes; }
    public void setMaxStorageBytes(long maxStorageBytes) { this.maxStorageBytes = maxStorageBytes; }
    public int getMaxEnrollments() { return maxEnrollments; }
    public void setMaxEnrollments(int maxEnrollments) { this.maxEnrollments = maxEnrollments; }
    public String getFeaturesJson() { return featuresJson; }
    public void setFeaturesJson(String featuresJson) { this.featuresJson = featuresJson; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
