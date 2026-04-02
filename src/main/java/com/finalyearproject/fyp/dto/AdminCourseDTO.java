package com.finalyearproject.fyp.dto;

public record AdminCourseDTO(
        Long   courseId,
        String courseName,
        String courseDesc,
        String teacherName,
        String teacherEmail,
        long   enrollmentCount,
        long   chapterCount
) {}