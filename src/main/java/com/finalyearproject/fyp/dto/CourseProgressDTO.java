package com.finalyearproject.fyp.dto;

import java.util.List;

public record CourseProgressDTO(
        Long                      courseId,
        String                    courseName,
        int                       enrolledCount,
        int                       classAvgPct,
        List<StudentProgressDTO>  students
) {}