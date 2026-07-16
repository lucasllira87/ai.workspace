package com.aiworkspace.audit.domain.model;

public record LearningStats(long enrolledCourses, long completedCourses, long certificatesCount) {

    public static LearningStats empty() {
        return new LearningStats(0, 0, 0);
    }
}
