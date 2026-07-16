package com.aiworkspace.documents.application.port.out;

public interface MimeTypeValidatorPort {

    /**
     * Detects the actual MIME type from file content (magic bytes), not from the declared header.
     * Returns "application/octet-stream" for unknown binary content.
     */
    String detect(byte[] content);
}
