package com.aiworkspace.billing.domain.model;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record BillingPeriod(Instant startAt, Instant endAt) {

    public BillingPeriod {
        Objects.requireNonNull(startAt, "startAt");
        Objects.requireNonNull(endAt, "endAt");
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("BillingPeriod endAt must be after startAt");
        }
    }

    public static BillingPeriod monthStartingNow() {
        ZonedDateTime start = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
        return new BillingPeriod(start.toInstant(), start.plusMonths(1).toInstant());
    }

    public static BillingPeriod trialPeriod(int trialDays) {
        Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return new BillingPeriod(start, start.plus(trialDays, ChronoUnit.DAYS));
    }

    public BillingPeriod next() {
        ZonedDateTime end = endAt.atZone(ZoneOffset.UTC);
        return new BillingPeriod(endAt, end.plusMonths(1).toInstant());
    }

    public boolean contains(Instant instant) {
        return !instant.isBefore(startAt) && instant.isBefore(endAt);
    }

    public boolean hasEnded() {
        return Instant.now().isAfter(endAt);
    }
}
