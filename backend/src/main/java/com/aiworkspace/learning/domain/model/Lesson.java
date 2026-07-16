package com.aiworkspace.learning.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Lesson {

    private final UUID id;
    private final UUID courseId;
    private String title;
    private String content;
    private String videoUrl;
    private final LessonType type;
    private int lessonOrder;
    private int estimatedMinutes;
    private List<QuizQuestion> quizQuestions;

    public Lesson(UUID id, UUID courseId, String title, String content, String videoUrl,
                  LessonType type, int lessonOrder, int estimatedMinutes,
                  List<QuizQuestion> quizQuestions) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
        this.type = type;
        this.lessonOrder = lessonOrder;
        this.estimatedMinutes = estimatedMinutes;
        this.quizQuestions = new ArrayList<>(quizQuestions);
    }

    void update(String title, String content, String videoUrl, int estimatedMinutes) {
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
        this.estimatedMinutes = estimatedMinutes;
    }

    void updateContent(String content) {
        this.content = content;
    }

    void setQuizQuestions(List<QuizQuestion> questions) {
        this.quizQuestions = new ArrayList<>(questions);
    }

    void setLessonOrder(int order) {
        this.lessonOrder = order;
    }

    public UUID getId() { return id; }
    public UUID getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getVideoUrl() { return videoUrl; }
    public LessonType getType() { return type; }
    public int getLessonOrder() { return lessonOrder; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public List<QuizQuestion> getQuizQuestions() { return Collections.unmodifiableList(quizQuestions); }
}
