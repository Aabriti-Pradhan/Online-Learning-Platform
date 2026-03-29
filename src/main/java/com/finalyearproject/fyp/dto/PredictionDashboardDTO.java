package com.finalyearproject.fyp.dto;

import java.util.List;

public record PredictionDashboardDTO(
        // 1. Score trend over time
        List<ScorePointDTO>       scoreTrend,
        // 2. Per-course performance (for radar chart)
        List<CoursePerformanceDTO> coursePerformance,
        // 3. Overall stats
        int    totalTestsTaken,
        int    overallAvgPct,
        int    totalQuestionsAnswered,
        int    totalCorrect,
        // 4. Activity heatmap data
        List<ActivityDayDTO>      activityData,
        // 5. AI-generated study roadmap and weak areas
        String studyPlan,
        String weakAreas,
        String motivationMessage,
        // Refresh flag
        boolean hasData
) {}