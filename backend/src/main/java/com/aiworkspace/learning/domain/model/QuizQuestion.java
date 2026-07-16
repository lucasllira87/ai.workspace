package com.aiworkspace.learning.domain.model;

import java.util.List;
import java.util.UUID;

public class QuizQuestion {

    private final UUID id;
    private final String question;
    private final List<String> options;
    private final int correctOptionIndex;
    private final String explanation;

    public QuizQuestion(UUID id, String question, List<String> options,
                         int correctOptionIndex, String explanation) {
        this.id = id;
        this.question = question;
        this.options = List.copyOf(options);
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
    }

    public UUID getId() { return id; }
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public String getExplanation() { return explanation; }
}
