package com.aiworkspace.aicore.application.port.out;

import java.util.Map;

public interface DocumentParser {

    boolean supports(String mimeType);

    ParsedDocument parse(byte[] content, String fileName);

    record ParsedDocument(
            String text,
            String mimeType,
            int pageCount,
            Map<String, Object> metadata
    ) {}
}
