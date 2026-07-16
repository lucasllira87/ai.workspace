package com.aiworkspace.documents.infrastructure.config;

import com.aiworkspace.documents.application.port.out.DocumentConfigPort;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "documents")
public class DocumentsProperties implements DocumentConfigPort {

    private long maxFileSizeBytes = 50L * 1024 * 1024; // 50 MB

    private Set<String> allowedMimeTypes = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/markdown",
            "text/x-markdown",
            "text/html",
            "text/plain"
    );

    private StorageProperties storage = new StorageProperties();

    private int ragTopK = 5;

    private double ragMinSimilarity = 0.7;

    private int stuckDocumentThresholdMinutes = 15;

    @Override
    public boolean isAllowedMimeType(String mimeType) {
        return allowedMimeTypes.contains(mimeType);
    }

    @Override
    public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
    public void setMaxFileSizeBytes(long maxFileSizeBytes) { this.maxFileSizeBytes = maxFileSizeBytes; }

    public Set<String> getAllowedMimeTypes() { return allowedMimeTypes; }
    public void setAllowedMimeTypes(Set<String> allowedMimeTypes) { this.allowedMimeTypes = allowedMimeTypes; }

    public StorageProperties getStorage() { return storage; }
    public void setStorage(StorageProperties storage) { this.storage = storage; }

    @Override
    public int getRagTopK() { return ragTopK; }
    public void setRagTopK(int ragTopK) { this.ragTopK = ragTopK; }

    @Override
    public double getRagMinSimilarity() { return ragMinSimilarity; }
    public void setRagMinSimilarity(double ragMinSimilarity) { this.ragMinSimilarity = ragMinSimilarity; }

    @Override
    public int getStuckDocumentThresholdMinutes() { return stuckDocumentThresholdMinutes; }
    public void setStuckDocumentThresholdMinutes(int v) { this.stuckDocumentThresholdMinutes = v; }

    public static class StorageProperties {
        private String provider = "local";
        private String localBasePath = "./data/uploads";

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getLocalBasePath() { return localBasePath; }
        public void setLocalBasePath(String localBasePath) { this.localBasePath = localBasePath; }
    }
}
