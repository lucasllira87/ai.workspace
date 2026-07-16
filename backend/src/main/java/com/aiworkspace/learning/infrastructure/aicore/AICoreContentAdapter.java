package com.aiworkspace.learning.infrastructure.aicore;

import com.aiworkspace.aicore.application.command.ChatCommand;
import com.aiworkspace.aicore.application.dto.ChatMessage;
import com.aiworkspace.aicore.application.port.in.ChatUseCase;
import com.aiworkspace.learning.application.port.out.AIContentPort;
import com.aiworkspace.learning.domain.model.CourseLevel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AICoreContentAdapter implements AIContentPort {

    private static final String MODULE = "learning";
    private static final String SYSTEM_PROMPT = """
            You are an expert educational content writer.
            Generate comprehensive, well-structured lesson content in Markdown format.
            Use headings, bullet points, code examples, and explanations appropriate for the given level.
            Write only the lesson content — no meta-commentary.
            """;

    private final ChatUseCase chatUseCase;

    public AICoreContentAdapter(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @Override
    public String generateLessonContent(String lessonTitle, String topic, CourseLevel level) {
        String userPrompt = """
                Generate comprehensive lesson content for:
                Title: %s
                Topic: %s
                Level: %s

                Write a complete lesson in Markdown with introduction, main concepts, examples, and a summary.
                """.formatted(lessonTitle, topic, level.name());

        ChatCommand command = ChatCommand.fromMessages(
                List.of(
                        ChatMessage.system(SYSTEM_PROMPT),
                        ChatMessage.user(userPrompt)
                ),
                null,
                MODULE,
                "system"
        );

        return chatUseCase.execute(command).content();
    }
}
