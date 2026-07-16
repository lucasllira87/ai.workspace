package com.aiworkspace.aicore.infrastructure.parser;

import com.aiworkspace.aicore.application.port.out.DocumentParser;
import com.aiworkspace.aicore.domain.exception.IngestionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class PdfDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String mimeType) {
        return "application/pdf".equalsIgnoreCase(mimeType);
    }

    @Override
    public ParsedDocument parse(byte[] content, String fileName) {
        try (PDDocument document = PDDocument.load(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            int pageCount = document.getNumberOfPages();
            return new ParsedDocument(text, "application/pdf", pageCount,
                    Map.of("fileName", fileName, "pageCount", pageCount));
        } catch (IOException e) {
            throw new IngestionException("Failed to parse PDF file: " + fileName, e);
        }
    }
}
