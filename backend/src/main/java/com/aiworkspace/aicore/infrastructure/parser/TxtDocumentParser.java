package com.aiworkspace.aicore.infrastructure.parser;

import com.aiworkspace.aicore.application.port.out.DocumentParser;
import com.aiworkspace.aicore.domain.exception.IngestionException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class TxtDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String mimeType) {
        return "text/plain".equalsIgnoreCase(mimeType);
    }

    @Override
    public ParsedDocument parse(byte[] content, String fileName) {
        try {
            String text = new String(content, StandardCharsets.UTF_8);
            return new ParsedDocument(text, "text/plain", 1, Map.of("fileName", fileName));
        } catch (Exception e) {
            throw new IngestionException("Failed to parse TXT file: " + fileName, e);
        }
    }
}
