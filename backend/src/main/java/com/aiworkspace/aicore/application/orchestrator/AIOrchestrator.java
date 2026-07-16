package com.aiworkspace.aicore.application.orchestrator;

import com.aiworkspace.aicore.application.dto.ChatRequest;
import com.aiworkspace.aicore.application.dto.ChatResponse;
import com.aiworkspace.aicore.application.dto.EmbeddingRequest;
import com.aiworkspace.aicore.application.dto.EmbeddingResponse;
import com.aiworkspace.aicore.application.dto.UsageMetricEvent;
import com.aiworkspace.aicore.application.port.out.UsageMetricsPort;
import com.aiworkspace.aicore.application.port.out.provider.ChatProvider;
import com.aiworkspace.aicore.application.port.out.provider.EmbeddingProvider;
import com.aiworkspace.aicore.domain.exception.AllProvidersExhaustedException;
import com.aiworkspace.aicore.domain.exception.ProviderUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Component
public class AIOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AIOrchestrator.class);

    private final FallbackChain fallbackChain;
    private final CostGuard costGuard;
    private final UsageMetricsPort metricsPort;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public AIOrchestrator(FallbackChain fallbackChain,
                           CostGuard costGuard,
                           UsageMetricsPort metricsPort,
                           RetryRegistry retryRegistry,
                           CircuitBreakerRegistry circuitBreakerRegistry) {
        this.fallbackChain = fallbackChain;
        this.costGuard = costGuard;
        this.metricsPort = metricsPort;
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public ChatResponse chat(ChatRequest request) {
        int estimatedInputTokens = request.messages() == null ? 1000
                : request.messages().stream().mapToInt(m -> m.content().length() / 4 + 1).sum();
        costGuard.checkEstimatedCost(request.model(), estimatedInputTokens);

        List<ChatProvider> providers = fallbackChain.getChatProvidersInOrder(request.model());
        if (providers.isEmpty()) {
            throw new AllProvidersExhaustedException(
                    "No chat providers available for model: " + request.model());
        }

        Exception lastException = null;
        for (ChatProvider provider : providers) {
            try {
                return executeChatWithResilience(provider, request);
            } catch (CallNotPermittedException e) {
                log.warn("Circuit breaker open for provider '{}', trying next", provider.getProviderId());
                lastException = e;
            } catch (ProviderUnavailableException e) {
                log.warn("Provider '{}' unavailable: {}, trying next", provider.getProviderId(), e.getMessage());
                lastException = e;
            }
        }
        throw new AllProvidersExhaustedException("All chat providers failed", lastException);
    }

    public EmbeddingResponse embed(EmbeddingRequest request) {
        int estimatedInputTokens = request.texts() == null ? 500
                : request.texts().stream().mapToInt(t -> t.length() / 4 + 1).sum();
        costGuard.checkEstimatedCost(request.model(), estimatedInputTokens);

        List<EmbeddingProvider> providers = fallbackChain.getEmbeddingProvidersInOrder(request.model());
        if (providers.isEmpty()) {
            throw new AllProvidersExhaustedException(
                    "No embedding providers available for model: " + request.model());
        }

        Exception lastException = null;
        for (EmbeddingProvider provider : providers) {
            try {
                return executeEmbeddingWithResilience(provider, request);
            } catch (CallNotPermittedException e) {
                log.warn("Circuit breaker open for provider '{}', trying next", provider.getProviderId());
                lastException = e;
            } catch (ProviderUnavailableException e) {
                log.warn("Provider '{}' unavailable: {}, trying next", provider.getProviderId(), e.getMessage());
                lastException = e;
            }
        }
        throw new AllProvidersExhaustedException("All embedding providers failed", lastException);
    }

    private ChatResponse executeChatWithResilience(ChatProvider provider, ChatRequest request) {
        String providerName = provider.getProviderId().value();
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(providerName);
        Retry retry = retryRegistry.retry("ai-provider");

        AtomicInteger attempts = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        Supplier<ChatResponse> decorated = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, () -> {
                    attempts.incrementAndGet();
                    return provider.chat(request);
                }));

        try {
            ChatResponse response = decorated.get();
            long latency = System.currentTimeMillis() - start;
            int retries = attempts.get() - 1;

            metricsPort.record(UsageMetricEvent.forChat(
                    provider.getProviderId(), response.model(),
                    response.usage(), response.estimatedCost(),
                    latency, retries,
                    (String) request.metadata().getOrDefault("module", "unknown"),
                    (String) request.metadata().getOrDefault("requestedBy", null)));

            return response;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            metricsPort.record(UsageMetricEvent.forError(
                    "chat", provider.getProviderId(), request.model(),
                    latency, attempts.get() - 1, e.getClass().getSimpleName(),
                    (String) request.metadata().getOrDefault("module", "unknown")));
            throw new ProviderUnavailableException(provider.getProviderId(), e.getMessage(), e);
        }
    }

    private EmbeddingResponse executeEmbeddingWithResilience(EmbeddingProvider provider,
                                                               EmbeddingRequest request) {
        String providerName = provider.getProviderId().value();
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(providerName);
        Retry retry = retryRegistry.retry("ai-provider");

        AtomicInteger attempts = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        Supplier<EmbeddingResponse> decorated = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, () -> {
                    attempts.incrementAndGet();
                    return provider.embed(request);
                }));

        try {
            EmbeddingResponse response = decorated.get();
            long latency = System.currentTimeMillis() - start;
            int retries = attempts.get() - 1;

            metricsPort.record(UsageMetricEvent.forEmbedding(
                    provider.getProviderId(), response.model(),
                    response.usage(), response.estimatedCost(),
                    latency, retries,
                    (String) request.metadata().getOrDefault("module", "unknown"),
                    (String) request.metadata().getOrDefault("requestedBy", null)));

            return response;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            metricsPort.record(UsageMetricEvent.forError(
                    "embed", provider.getProviderId(), request.model(),
                    latency, attempts.get() - 1, e.getClass().getSimpleName(),
                    (String) request.metadata().getOrDefault("module", "unknown")));
            throw new ProviderUnavailableException(provider.getProviderId(), e.getMessage(), e);
        }
    }
}
