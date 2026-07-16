package com.aiworkspace.billing.domain;

import com.aiworkspace.billing.domain.event.SubscriptionActivatedEvent;
import com.aiworkspace.billing.domain.event.SubscriptionCanceledEvent;
import com.aiworkspace.billing.domain.event.SubscriptionExpiredEvent;
import com.aiworkspace.billing.domain.event.SubscriptionRenewedEvent;
import com.aiworkspace.billing.domain.event.TrialStartedEvent;
import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Subscription;
import com.aiworkspace.billing.domain.model.SubscriptionStatus;
import com.aiworkspace.shared.domain.DomainEvent;
import com.aiworkspace.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PLAN_ID = UUID.randomUUID();
    private static final UUID SUB_ID = UUID.randomUUID();

    @Test
    void startTrial_createsTrialSubscriptionWithEvent() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);

        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.TRIAL);
        assertThat(sub.getUserId()).isEqualTo(USER_ID);
        assertThat(sub.getPlanId()).isEqualTo(PLAN_ID);
        assertThat(sub.isAccessible()).isTrue();

        List<DomainEvent> events = sub.getDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(TrialStartedEvent.class);
    }

    @Test
    void startTrial_periodIs14Days() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);

        long days = java.time.temporal.ChronoUnit.DAYS.between(
                sub.getCurrentPeriod().startAt(), sub.getCurrentPeriod().endAt());
        assertThat(days).isEqualTo(14);
    }

    @Test
    void activate_fromTrial_setsActiveStatusAndEmitsEvent() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);
        sub.clearDomainEvents();
        UUID newPlanId = UUID.randomUUID();
        BillingPeriod period = BillingPeriod.monthStartingNow();

        sub.activate(newPlanId, "ext-123", period);

        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.getPlanId()).isEqualTo(newPlanId);
        assertThat(sub.getExternalId()).isEqualTo("ext-123");

        assertThat(sub.getDomainEvents()).hasSize(1);
        assertThat(sub.getDomainEvents().get(0)).isInstanceOf(SubscriptionActivatedEvent.class);
    }

    @Test
    void activate_throwsWhenAlreadyCanceled() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);
        sub.cancel();

        assertThatThrownBy(() -> sub.activate(UUID.randomUUID(), "ext", BillingPeriod.monthStartingNow()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("canceled");
    }

    @Test
    void cancel_setsStatusAndEmitsEvent() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);
        sub.clearDomainEvents();

        sub.cancel();

        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        assertThat(sub.getCanceledAt()).isNotNull();
        assertThat(sub.isAccessible()).isFalse();

        assertThat(sub.getDomainEvents()).hasSize(1);
        assertThat(sub.getDomainEvents().get(0)).isInstanceOf(SubscriptionCanceledEvent.class);
    }

    @Test
    void cancel_throwsWhenAlreadyCanceled() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);
        sub.cancel();

        assertThatThrownBy(sub::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already canceled");
    }

    @Test
    void renew_fromActive_updatesperiodAndEmitsEvent() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);
        sub.activate(PLAN_ID, "ext", BillingPeriod.monthStartingNow());
        sub.clearDomainEvents();
        BillingPeriod next = sub.getCurrentPeriod().next();

        sub.renew(next);

        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.getCurrentPeriod()).isEqualTo(next);
        assertThat(sub.getDomainEvents().get(0)).isInstanceOf(SubscriptionRenewedEvent.class);
    }

    @Test
    void renew_throwsFromTrialStatus() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);

        assertThatThrownBy(() -> sub.renew(BillingPeriod.monthStartingNow()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cannot renew");
    }

    @Test
    void expire_setsExpiredStatusAndEmitsEvent() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);
        sub.clearDomainEvents();

        sub.expire();

        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(sub.isAccessible()).isFalse();
        assertThat(sub.getDomainEvents().get(0)).isInstanceOf(SubscriptionExpiredEvent.class);
    }

    @Test
    void expire_isIdempotentWhenAlreadyExpired() {
        Subscription sub = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);
        sub.expire();
        sub.clearDomainEvents();

        assertThatCode(sub::expire).doesNotThrowAnyException();
        assertThat(sub.getDomainEvents()).isEmpty();
    }

    @Test
    void isAccessible_returnsTrueForTrialActiveAndPastDue() {
        Subscription trial = Subscription.startTrial(SUB_ID, USER_ID, PLAN_ID);
        assertThat(trial.isAccessible()).isTrue();

        trial.activate(PLAN_ID, "ext", BillingPeriod.monthStartingNow());
        assertThat(trial.isAccessible()).isTrue();

        trial.markPastDue();
        assertThat(trial.isAccessible()).isTrue();
    }

    @Test
    void startTrial_requiresNonNullArguments() {
        assertThatThrownBy(() -> Subscription.startTrial(null, USER_ID, PLAN_ID))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Subscription.startTrial(SUB_ID, null, PLAN_ID))
                .isInstanceOf(NullPointerException.class);
    }
}
