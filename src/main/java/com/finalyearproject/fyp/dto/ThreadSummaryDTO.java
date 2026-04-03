package com.finalyearproject.fyp.dto;

import java.time.LocalDateTime;
import java.util.List;

// Thread summary shown on the discussion list page
public record ThreadSummaryDTO(
        Long postId,
        String title,
        String content,
        String authorUsername,
        Long authorId,
        LocalDateTime createdAt,
        int replyCount,
        List<String> mentionedUsernames,
        List<String> mentionedCourseNames
) {}