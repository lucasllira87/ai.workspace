package com.aiworkspace.documents.application.port.out;

public interface DocumentConfigPort {

    long getMaxFileSizeBytes();

    boolean isAllowedMimeType(String mimeType);

    int getRagTopK();

    double getRagMinSimilarity();

    int getStuckDocumentThresholdMinutes();
}
