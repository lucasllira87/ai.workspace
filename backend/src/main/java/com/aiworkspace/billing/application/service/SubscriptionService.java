package com.aiworkspace.billing.application.service;

import com.aiworkspace.billing.application.command.UpgradeSubscriptionCommand;
import com.aiworkspace.billing.application.dto.PlanDto;
import com.aiworkspace.billing.application.dto.SubscriptionDto;
import com.aiworkspace.billing.application.port.in.CancelSubscriptionUseCase;
import com.aiworkspace.billing.application.port.in.GetOrCreateTrialUseCase;
import com.aiworkspace.billing.application.port.in.GetSubscriptionUseCase;
import com.aiworkspace.billing.application.port.in.ListPlansUseCase;
import com.aiworkspace.billing.application.port.in.UpgradeSubscriptionUseCase;
import com.aiworkspace.billing.application.port.out.PaymentGatewayPort;
import com.aiworkspace.billing.application.port.out.PlanRepository;
import com.aiworkspace.billing.application.port.out.SubscriptionRepository;
import com.aiworkspace.billing.domain.exception.PlanNotFoundException;
import com.aiworkspace.billing.domain.exception.SubscriptionNotFoundException;
import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Plan;
import com.aiworkspace.billing.domain.model.Subscription;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService implements GetOrCreateTrialUseCase, UpgradeSubscriptionUseCase,
        CancelSubscriptionUseCase, GetSubscriptionUseCase, ListPlansUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PaymentGatewayPort paymentGateway;
    private final ApplicationEventPublisher eventPublisher;
    private final Counter trialCreatedCounter;
    private final Counter upgradeCounter;
    private final Counter cancelCounter;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                                PlanRepository planRepository,
                                PaymentGatewayPort paymentGateway,
                                ApplicationEventPublisher eventPublisher,
                                MeterRegistry meterRegistry) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
        this.trialCreatedCounter = Counter.builder("billing.trials.created")
                .description("Total trial subscriptions started").register(meterRegistry);
        this.upgradeCounter = Counter.builder("billing.subscriptions.upgraded")
                .description("Total subscription upgrades").register(meterRegistry);
        this.cancelCounter = Counter.builder("billing.subscriptions.canceled")
                .description("Total subscription cancellations").register(meterRegistry);
    }

    @Override
    @Transactional
    public SubscriptionDto getOrCreateTrial(UUID userId) {
        return subscriptionRepository.findActiveByUserId(userId)
                .map(SubscriptionDto::from)
                .orElseGet(() -> {
                    Plan freePlan = planRepository.findFreePlan()
                            .orElseThrow(() -> new PlanNotFoundException("FREE"));
                    Subscription trial = Subscription.startTrial(UUID.randomUUID(), userId, freePlan.getId());
                    Subscription saved = subscriptionRepository.save(trial);
                    trial.getDomainEvents().forEach(eventPublisher::publishEvent);
                    trial.clearDomainEvents();
                    trialCreatedCounter.increment();
                    return SubscriptionDto.from(saved);
                });
    }

    @Override
    @Transactional
    public SubscriptionDto upgrade(UpgradeSubscriptionCommand command) {
        Plan plan = planRepository.findByName(command.planName())
                .orElseThrow(() -> new PlanNotFoundException(command.planName()));

        Subscription subscription = subscriptionRepository.findActiveByUserId(command.userId())
                .orElseGet(() -> {
                    Plan freePlan = planRepository.findFreePlan()
                            .orElseThrow(() -> new PlanNotFoundException("FREE"));
                    return Subscription.startTrial(UUID.randomUUID(), command.userId(), freePlan.getId());
                });

        PaymentGatewayPort.GatewaySubscription gateway = paymentGateway.createSubscription(
                command.userId(), plan.getName(), command.paymentMethodId());

        subscription.activate(plan.getId(), gateway.externalId(), gateway.period());
        Subscription saved = subscriptionRepository.save(subscription);
        subscription.getDomainEvents().forEach(eventPublisher::publishEvent);
        subscription.clearDomainEvents();
        upgradeCounter.increment();
        return SubscriptionDto.from(saved);
    }

    @Override
    @Transactional
    public void cancel(UUID userId) {
        Subscription subscription = subscriptionRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new SubscriptionNotFoundException(userId));

        if (subscription.getExternalId() != null) {
            paymentGateway.cancelSubscription(subscription.getExternalId());
        }
        subscription.cancel();
        subscriptionRepository.save(subscription);
        subscription.getDomainEvents().forEach(eventPublisher::publishEvent);
        subscription.clearDomainEvents();
        cancelCounter.increment();
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDto getByUserId(UUID userId) {
        return subscriptionRepository.findActiveByUserId(userId)
                .map(SubscriptionDto::from)
                .orElseThrow(() -> new SubscriptionNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanDto> listActive() {
        return planRepository.findAllActive().stream().map(PlanDto::from).toList();
    }
}
