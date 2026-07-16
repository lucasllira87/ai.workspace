package com.aiworkspace.learning.infrastructure.aicore;

import com.aiworkspace.aicore.application.command.ChatCommand;
import com.aiworkspace.aicore.application.dto.ChatMessage;
import com.aiworkspace.aicore.application.port.in.ChatUseCase;
import com.aiworkspace.learning.application.port.out.AIQuizPort;
import com.aiworkspace.learning.domain.model.QuizQuestion;
import com.aiworkspace.shared.exception.DomainException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AICoreQuizAdapter implements AIQuizPort {

    private static final String MODULE = "learning";
    private static final String SYSTEM_PROMPT = """
            You are an expert at creating educational assessments.
            Always respond with a valid JSON array of quiz questions and nothing else.
            Each object must have: question (string), options (array of 4 strings), correctOptionIndex (0-3), explanation (string).
            """;

    private final ChatUseCase chatUseCase;
    private final ObjectMapper objectMapper;

    public AICoreQuizAdapter(ChatUseCase chatUseCase, ObjectMapper objectMapper) {
        this.chatUseCase = chatUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<QuizQuestion> generateQuestions(String lessonContent, String lessonTitle, int count) {
        String userPrompt = """
                Generate %d multiple-choice quiz questions for the following lesson.

                Lesson Title: %s

                Lesson Content:
                %s

                Respond with a JSON array only. No explanation text outside the JSON.
                """.formatted(count, lessonTitle, lessonContent);

        ChatCommand command = ChatCommand.fromMessages(
                List.of(
                        ChatMessage.system(SYSTEM_PROMPT),
                        ChatMessage.user(userPrompt)
                ),
                null,
                MODULE,
                "system"
        );

        String response = chatUseCase.execute(command).content();
        return parseQuizQuestions(response);
    }

    @SuppressWarnings("unchecked")
    private List<QuizQuestion> parseQuizQuestions(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) {
            throw new DomainException("AI returned an invalid quiz format — no JSON array found");
        }

        String json = response.substring(start, end + 1);
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream().map(this::toQuizQuestion).toList();
        } catch (Exception e) {
            throw new DomainException("AI returned malformed quiz JSON: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private QuizQuestion toQuizQuestion(Map<String, Object> map) {
        String question = (String) map.get("question");
        List<String> options = (List<String>) map.get("options");
        Number correctIndex = (Number) map.get("correctOptionIndex");

        if (question == null || options == null || correctIndex == null) {
            throw new DomainException(
                    "AI returned a quiz question missing required fields: question, options, or correctOptionIndex");
        }
        if (options.size() < 2) {
            throw new DomainException("AI returned a quiz question with fewer than 2 options");
        }

        return new QuizQuestion(
                UUID.randomUUID(),
                question,
                options,
                correctIndex.intValue(),
                (String) map.get("explanation")
        );
    }
}
