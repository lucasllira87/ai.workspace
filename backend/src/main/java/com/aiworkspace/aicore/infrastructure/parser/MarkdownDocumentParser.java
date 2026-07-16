package com.aiworkspace.aicore.infrastructure.parser;

import com.aiworkspace.aicore.application.port.out.DocumentParser;
import com.aiworkspace.aicore.domain.exception.IngestionException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class MarkdownDocumentParser implements DocumentParser {

    // Strips markdown formatting to get plain text for embedding
    private static final Pattern CODE_BLOCK = Pattern.compile("```[\\s\\S]*?```");
    private static final Pattern INLINE_CODE = Pattern.compile("`[^`]+`");
    private static final Pattern HEADERS = Pattern.compile("^#{1,6}\\s+", Pattern.MULTILINE);
    private static final Pattern BOLD_ITALIC = Pattern.compile("[*_]{1,3}([^*_]+)[*_]{1,3}");
    private static final Pattern LINKS = Pattern.compile("!?\\[([^\\]]+)]\\([^)]+\\)");
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");

    @Override
    public boolean supports(String mimeType) {
        return "text/markdown".equalsIgnoreCase(mimeType)
                || "text/x-markdown".equalsIgnoreCase(mimeType);
    }

    @Override
    public ParsedDocument parse(byte[] content, String fileName) {
        try {
            // We preserve the raw markdown as text — chunking strategy will use structure
            String markdown = new String(content, StandardCharsets.UTF_8);
            return new ParsedDocument(markdown, "text/markdown", 1,
                    Map.of("fileName", fileName, "format", "markdown"));
        } catch (Exception e) {
            throw new IngestionException("Failed to parse Markdown file: " + fileName, e);
        }
    }

    public static String stripMarkdown(String markdown) {
        String text = CODE_BLOCK.matcher(markdown).replaceAll(" ");
        text = INLINE_CODE.matcher(text).replaceAll("$1");
        text = HEADERS.matcher(text).replaceAll("");
        text = BOLD_ITALIC.matcher(text).replaceAll("$1");
        text = LINKS.matcher(text).replaceAll("$1");
        text = HTML_TAGS.matcher(text).replaceAll("");
        return text.replaceAll("\\s+", " ").trim();
    }
}
