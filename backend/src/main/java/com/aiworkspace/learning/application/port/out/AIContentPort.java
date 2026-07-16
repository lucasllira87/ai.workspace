package com.aiworkspace.learning.application.port.out;

import com.aiworkspace.learning.domain.model.CourseLevel;

public interface AIContentPort {
    String generateLessonContent(String lessonTitle, String topic, CourseLevel level);
}
