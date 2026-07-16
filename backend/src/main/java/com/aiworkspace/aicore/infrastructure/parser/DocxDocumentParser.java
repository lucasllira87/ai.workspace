package com.aiworkspace.aicore.infrastructure.parser;

import com.aiworkspace.aicore.application.port.out.DocumentParser;
import com.aiworkspace.aicore.domain.exception.IngestionException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

@Component
public class DocxDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                .equalsIgnoreCase(mimeType);
    }

    @Override
    public ParsedDocument parse(byte[] content, String fileName) {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(content));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            return new ParsedDocument(text,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    1, Map.of("fileName", fileName));
        } catch (IOException e) {
            throw new IngestionException("Failed to parse DOCX file: " + fileName, e);
        }
    }
}
