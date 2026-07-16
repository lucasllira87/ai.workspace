package com.aiworkspace.billing.presentation.controller;

import com.aiworkspace.billing.application.command.UpgradeSubscriptionCommand;
import com.aiworkspace.billing.application.dto.InvoiceDto;
import com.aiworkspace.billing.application.dto.PlanDto;
import com.aiworkspace.billing.application.dto.QuotaStatusDto;
import com.aiworkspace.billing.application.dto.SubscriptionDto;
import com.aiworkspace.billing.application.dto.UsageSummaryDto;
import com.aiworkspace.billing.application.port.in.CancelSubscriptionUseCase;
import com.aiworkspace.billing.application.port.in.CheckQuotaUseCase;
import com.aiworkspace.billing.application.port.in.GetInvoiceUseCase;
import com.aiworkspace.billing.application.port.in.GetOrCreateTrialUseCase;
import com.aiworkspace.billing.application.port.in.GetSubscriptionUseCase;
import com.aiworkspace.billing.application.port.in.GetUsageSummaryUseCase;
import com.aiworkspace.billing.application.port.in.ListPlansUseCase;
import com.aiworkspace.billing.application.port.in.UpgradeSubscriptionUseCase;
import com.aiworkspace.billing.application.port.out.BillingUserContextPort;
import com.aiworkspace.billing.presentation.request.UpgradeSubscriptionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final GetOrCreateTrialUseCase getOrCreateTrial;
    private final UpgradeSubscriptionUseCase upgradeSubscription;
    private final CancelSubscriptionUseCase cancelSubscription;
    private final GetSubscriptionUseCase getSubscription;
    private final ListPlansUseCase listPlans;
    private final CheckQuotaUseCase checkQuota;
    private final GetUsageSummaryUseCase getUsageSummary;
    private final GetInvoiceUseCase getInvoice;
    private final BillingUserContextPort userContext;

    public BillingController(GetOrCreateTrialUseCase getOrCreateTrial,
                              UpgradeSubscriptionUseCase upgradeSubscription,
                              CancelSubscriptionUseCase cancelSubscription,
                              GetSubscriptionUseCase getSubscription,
                              ListPlansUseCase listPlans,
                              CheckQuotaUseCase checkQuota,
                              GetUsageSummaryUseCase getUsageSummary,
                              GetInvoiceUseCase getInvoice,
                              BillingUserContextPort userContext) {
        this.getOrCreateTrial = getOrCreateTrial;
        this.upgradeSubscription = upgradeSubscription;
        this.cancelSubscription = cancelSubscription;
        this.getSubscription = getSubscription;
        this.listPlans = listPlans;
        this.checkQuota = checkQuota;
        this.getUsageSummary = getUsageSummary;
        this.getInvoice = getInvoice;
        this.userContext = userContext;
    }

    @GetMapping("/plans")
    public List<PlanDto> listPlans() {
        return listPlans.listActive();
    }

    @GetMapping("/subscription")
    public SubscriptionDto getSubscription() {
        UUID userId = userContext.getCurrentUserId();
        return getOrCreateTrial.getOrCreateTrial(userId);
    }

    @PostMapping("/subscription/upgrade")
    public SubscriptionDto upgrade(@Valid @RequestBody UpgradeSubscriptionRequest request) {
        UUID userId = userContext.getCurrentUserId();
        return upgradeSubscription.upgrade(
                new UpgradeSubscriptionCommand(userId, request.planName(), request.paymentMethodId()));
    }

    @DeleteMapping("/subscription")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel() {
        UUID userId = userContext.getCurrentUserId();
        cancelSubscription.cancel(userId);
    }

    @GetMapping("/usage")
    public UsageSummaryDto getUsage() {
        UUID userId = userContext.getCurrentUserId();
        return getUsageSummary.getCurrentPeriod(userId);
    }

    @GetMapping("/quota")
    public QuotaStatusDto getQuota() {
        UUID userId = userContext.getCurrentUserId();
        return checkQuota.check(userId);
    }

    @GetMapping("/invoices")
    public List<InvoiceDto> listInvoices() {
        UUID userId = userContext.getCurrentUserId();
        return getInvoice.listByUser(userId);
    }

    @GetMapping("/invoices/{invoiceId}")
    public InvoiceDto getInvoice(@PathVariable UUID invoiceId) {
        UUID userId = userContext.getCurrentUserId();
        return getInvoice.getById(invoiceId, userId);
    }
}
