package com.aiworkspace.learning.presentation.controller;

import com.aiworkspace.learning.application.command.AskTutorCommand;
import com.aiworkspace.learning.application.port.in.AskTutorUseCase;
import com.aiworkspace.learning.application.port.out.LearningUserContextPort;
import com.aiworkspace.learning.presentation.request.AskTutorRequest;
import com.aiworkspace.learning.presentation.response.TutorResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments/{enrollmentId}/tutor")
public class TutorController {

    private final AskTutorUseCase askTutor;
    private final LearningUserContextPort userContext;

    public TutorController(AskTutorUseCase askTutor, LearningUserContextPort userContext) {
        this.askTutor = askTutor;
        this.userContext = userContext;
    }

    @PostMapping
    public TutorResponse ask(@PathVariable UUID enrollmentId,
                              @RequestParam UUID lessonId,
                              @Valid @RequestBody AskTutorRequest request) {
        UUID userId = userContext.getCurrentUserId();
        var answer = askTutor.ask(new AskTutorCommand(enrollmentId, lessonId, userId, request.question()));
        return new TutorResponse(answer.lessonId(), answer.question(), answer.answer());
    }
}
