package com.aiworkspace.learning.application.command;

import com.aiworkspace.learning.domain.model.CourseLevel;

import java.util.UUID;

public record CreateCourseCommand(UUID ownerId, String title, String description,
                                   CourseLevel level, String category) {}
