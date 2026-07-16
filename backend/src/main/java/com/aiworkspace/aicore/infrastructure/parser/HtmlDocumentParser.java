package com.aiworkspace.aicore.infrastructure.parser;

import com.aiworkspace.aicore.application.port.out.DocumentParser;
import com.aiworkspace.aicore.domain.exception.IngestionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class HtmlDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String mimeType) {
        return "text/html".equalsIgnoreCase(mimeType);
    }

    @Override
    public ParsedDocument parse(byte[] content, String fileName) {
        try {
            String html = new String(content, StandardCharsets.UTF_8);
            Document doc = Jsoup.parse(html);
            doc.select("script, style, nav, footer, header").remove();
            String text = doc.body().text();
            return new ParsedDocument(text, "text/html", 1, Map.of("fileName", fileName));
        } catch (Exception e) {
            throw new IngestionException("Failed to parse HTML file: " + fileName, e);
        }
    }
}
