package com.aiworkspace.billing.application;

import com.aiworkspace.billing.application.command.UpgradeSubscriptionCommand;
import com.aiworkspace.billing.application.dto.SubscriptionDto;
import com.aiworkspace.billing.application.port.out.PaymentGatewayPort;
import com.aiworkspace.billing.application.port.out.PlanRepository;
import com.aiworkspace.billing.application.port.out.SubscriptionRepository;
import com.aiworkspace.billing.application.service.SubscriptionService;
import com.aiworkspace.billing.domain.exception.PlanNotFoundException;
import com.aiworkspace.billing.domain.exception.SubscriptionNotFoundException;
import com.aiworkspace.billing.domain.model.BillingCycle;
import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Plan;
import com.aiworkspace.billing.domain.model.PlanQuota;
import com.aiworkspace.billing.domain.model.Subscription;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock SubscriptionRepository subscriptionRepository;
    @Mock PlanRepository planRepository;
    @Mock PaymentGatewayPort paymentGateway;
    @Mock ApplicationEventPublisher eventPublisher;

    SubscriptionService service;

    // PlanQuota(maxTokensPerMonth, maxDocuments, maxStorageBytes, maxEnrollments)
    private static final PlanQuota DEFAULT_QUOTA = new PlanQuota(100_000L, 100, 10L * 1024 * 1024 * 1024, 10);

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(subscriptionRepository, planRepository,
                paymentGateway, eventPublisher, new SimpleMeterRegistry());
    }

    @Test
    void getOrCreateTrial_returnsExistingSubscriptionWhenPresent() {
        Subscription existing = Subscription.startTrial(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(subscriptionRepository.findActiveByUserId(any())).thenReturn(Optional.of(existing));

        SubscriptionDto result = service.getOrCreateTrial(UUID.randomUUID());

        assertThat(result.status()).isEqualTo("TRIAL");
        verify(planRepository, never()).findFreePlan();
    }

    @Test
    void getOrCreateTrial_createsTrialWhenNoExistingSubscription() {
        UUID userId = UUID.randomUUID();
        UUID freePlanId = UUID.randomUUID();
        Plan freePlan = freePlan(freePlanId);
        when(subscriptionRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());
        when(planRepository.findFreePlan()).thenReturn(Optional.of(freePlan));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionDto result = service.getOrCreateTrial(userId);

        assertThat(result.status()).isEqualTo("TRIAL");
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(eventPublisher, atLeastOnce()).publishEvent(any());
    }

    @Test
    void getOrCreateTrial_throwsWhenFreePlanNotFound() {
        when(subscriptionRepository.findActiveByUserId(any())).thenReturn(Optional.empty());
        when(planRepository.findFreePlan()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrCreateTrial(UUID.randomUUID()))
                .isInstanceOf(PlanNotFoundException.class);
    }

    @Test
    void cancel_cancelsSubscriptionAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        Subscription sub = Subscription.startTrial(UUID.randomUUID(), userId, UUID.randomUUID());
        when(subscriptionRepository.findActiveByUserId(userId)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.cancel(userId);

        assertThat(sub.getStatus().name()).isEqualTo("CANCELED");
        verify(eventPublisher, atLeastOnce()).publishEvent(any());
    }

    @Test
    void cancel_throwsWhenNoActiveSubscription() {
        UUID userId = UUID.randomUUID();
        when(subscriptionRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(userId))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    @Test
    void upgrade_activatesSubscriptionViaPlan() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        Plan plan = proPlan(planId);
        Subscription sub = Subscription.startTrial(UUID.randomUUID(), userId, UUID.randomUUID());
        BillingPeriod period = BillingPeriod.monthStartingNow();
        PaymentGatewayPort.GatewaySubscription gateway = new PaymentGatewayPort.GatewaySubscription("ext-123", period);

        when(planRepository.findByName("PRO")).thenReturn(Optional.of(plan));
        when(subscriptionRepository.findActiveByUserId(userId)).thenReturn(Optional.of(sub));
        when(paymentGateway.createSubscription(any(), any(), any())).thenReturn(gateway);
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionDto result = service.upgrade(new UpgradeSubscriptionCommand(userId, "PRO", "pm_123"));

        assertThat(result.status()).isEqualTo("ACTIVE");
        verify(eventPublisher, atLeastOnce()).publishEvent(any());
    }

    private Plan freePlan(UUID id) {
        return Plan.reconstitute(id, "FREE", null, BillingCycle.MONTHLY,
                DEFAULT_QUOTA, Set.of(), true);
    }

    private Plan proPlan(UUID id) {
        return Plan.reconstitute(id, "PRO", null, BillingCycle.MONTHLY,
                DEFAULT_QUOTA, Set.of(), true);
    }
}
