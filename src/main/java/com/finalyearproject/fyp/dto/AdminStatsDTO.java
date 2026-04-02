package com.finalyearproject.fyp.dto;

public record AdminStatsDTO(
        long totalUsers,
        long totalStudents,
        long totalTeachers,
        long totalCourses,
        long totalEnrollments,
        long totalTests
) {}