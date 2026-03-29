package com.finalyearproject.fyp.dto;

public record MotivationDTO(
        String message,       // short motivational text
        int    overallPct,    // overall score percentage
        int    weeklyPct,     // this week's average
        int    prevWeeklyPct, // last week's average
        String trend          // UP, DOWN, STABLE
) {}