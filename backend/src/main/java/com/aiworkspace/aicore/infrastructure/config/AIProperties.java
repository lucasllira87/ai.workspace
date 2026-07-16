package com.aiworkspace.aicore.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "ai")
public class AIProperties {

    private String defaultChatModel = "gpt-4o";
    private String defaultEmbeddingModel = "text-embedding-3-small";
    private ProvidersConfig providers = new ProvidersConfig();
    private FallbackOrderConfig fallbackOrder = new FallbackOrderConfig();
    private CostGuardConfig costGuard = new CostGuardConfig();
    private IngestionConfig ingestion = new IngestionConfig();

    public static class ProvidersConfig {
        private ProviderConfig openai = new ProviderConfig();
        private ProviderConfig anthropic = new ProviderConfig();
        private ProviderConfig google = new ProviderConfig();
        private OllamaConfig ollama = new OllamaConfig();

        public ProviderConfig getOpenai() { return openai; }
        public void setOpenai(ProviderConfig openai) { this.openai = openai; }
        public ProviderConfig getAnthropic() { return anthropic; }
        public void setAnthropic(ProviderConfig anthropic) { this.anthropic = anthropic; }
        public ProviderConfig getGoogle() { return google; }
        public void setGoogle(ProviderConfig google) { this.google = google; }
        public OllamaConfig getOllama() { return ollama; }
        public void setOllama(OllamaConfig ollama) { this.ollama = ollama; }
    }

    public static class ProviderConfig {
        private boolean enabled = false;
        private String apiKey;
        private String baseUrl;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class OllamaConfig {
        private boolean enabled = false;
        private String baseUrl = "http://localhost:11434";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class FallbackOrderConfig {
        private List<String> chat = List.of("openai", "anthropic", "google");
        private List<String> embedding = List.of("openai", "google");

        public List<String> getChat() { return chat; }
        public void setChat(List<String> chat) { this.chat = chat; }
        public List<String> getEmbedding() { return embedding; }
        public void setEmbedding(List<String> embedding) { this.embedding = embedding; }
    }

    public static class CostGuardConfig {
        private boolean enabled = true;
        private BigDecimal maxCostPerRequest = new BigDecimal("0.10");

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public BigDecimal getMaxCostPerRequest() { return maxCostPerRequest; }
        public void setMaxCostPerRequest(BigDecimal max) { this.maxCostPerRequest = max; }
    }

    public static class IngestionConfig {
        private int chunkSize = 500;
        private int chunkOverlap = 50;
        private int embeddingBatchSize = 100;

        public int getChunkSize() { return chunkSize; }
        public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
        public int getChunkOverlap() { return chunkOverlap; }
        public void setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; }
        public int getEmbeddingBatchSize() { return embeddingBatchSize; }
        public void setEmbeddingBatchSize(int size) { this.embeddingBatchSize = size; }
    }

    // Getters and setters for top-level fields
    public String getDefaultChatModel() { return defaultChatModel; }
    public void setDefaultChatModel(String m) { this.defaultChatModel = m; }
    public String getDefaultEmbeddingModel() { return defaultEmbeddingModel; }
    public void setDefaultEmbeddingModel(String m) { this.defaultEmbeddingModel = m; }
    public ProvidersConfig getProviders() { return providers; }
    public void setProviders(ProvidersConfig providers) { this.providers = providers; }
    public FallbackOrderConfig getFallbackOrder() { return fallbackOrder; }
    public void setFallbackOrder(FallbackOrderConfig fallbackOrder) { this.fallbackOrder = fallbackOrder; }
    public CostGuardConfig getCostGuard() { return costGuard; }
    public void setCostGuard(CostGuardConfig costGuard) { this.costGuard = costGuard; }
    public IngestionConfig getIngestion() { return ingestion; }
    public void setIngestion(IngestionConfig ingestion) { this.ingestion = ingestion; }
}
