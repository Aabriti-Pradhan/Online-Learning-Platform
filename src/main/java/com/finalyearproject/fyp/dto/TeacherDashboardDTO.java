package com.finalyearproject.fyp.dto;

import java.util.List;

public record TeacherDashboardDTO(
        List<CourseProgressDTO> courses,
        int                     totalStudents,
        int                     overallClassAvg,
        boolean                 hasData
) {}