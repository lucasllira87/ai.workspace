package com.aiworkspace.aicore.infrastructure.config;

import com.aiworkspace.aicore.infrastructure.provider.ModelPricingCatalog;
import com.aiworkspace.aicore.infrastructure.provider.anthropic.AnthropicChatAdapter;
import com.aiworkspace.aicore.infrastructure.provider.google.GoogleChatAdapter;
import com.aiworkspace.aicore.infrastructure.provider.openai.OpenAIChatAdapter;
import com.aiworkspace.aicore.infrastructure.provider.openai.OpenAIEmbeddingAdapter;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AIProviderConfig {

    // ======= OpenAI =======

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.openai", name = "enabled", havingValue = "true")
    public OpenAIClient openAIClient(AIProperties properties) {
        return OpenAIOkHttpClient.builder()
                .apiKey(properties.getProviders().getOpenai().getApiKey())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.openai", name = "enabled", havingValue = "true")
    public OpenAIChatAdapter openAIChatAdapter(OpenAIClient client,
                                                ModelPricingCatalog pricingCatalog) {
        return new OpenAIChatAdapter(client, pricingCatalog);
    }

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.openai", name = "enabled", havingValue = "true")
    public OpenAIEmbeddingAdapter openAIEmbeddingAdapter(OpenAIClient client,
                                                          ModelPricingCatalog pricingCatalog) {
        return new OpenAIEmbeddingAdapter(client, pricingCatalog);
    }

    // ======= Anthropic =======

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.anthropic", name = "enabled", havingValue = "true")
    public AnthropicClient anthropicClient(AIProperties properties) {
        return AnthropicOkHttpClient.builder()
                .apiKey(properties.getProviders().getAnthropic().getApiKey())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.anthropic", name = "enabled", havingValue = "true")
    public AnthropicChatAdapter anthropicChatAdapter(AnthropicClient client,
                                                      ModelPricingCatalog pricingCatalog) {
        return new AnthropicChatAdapter(client, pricingCatalog);
    }

    // ======= Google Gemini (via REST) =======

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.google", name = "enabled", havingValue = "true")
    public GoogleChatAdapter googleChatAdapter(AIProperties properties,
                                                ModelPricingCatalog pricingCatalog) {
        RestClient restClient = RestClient.builder().build();
        return new GoogleChatAdapter(restClient,
                properties.getProviders().getGoogle().getApiKey(), pricingCatalog);
    }
}
