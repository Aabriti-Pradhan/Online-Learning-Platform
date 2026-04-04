package com.finalyearproject.fyp.dto;

import java.util.List;

// Incoming request when teacher creates or edits an announcement
public record CreateAnnouncementRequest(
        String     title,
        String     content,
        List<Long> taggedChapterIds   // IDs of chapters mentioned with #
) {}