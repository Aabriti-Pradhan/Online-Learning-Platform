package com.finalyearproject.fyp.dto;

import java.time.LocalDateTime;
import java.util.List;

// Returned to the view for rendering an announcement
public record AnnouncementDTO(
        Long            announcementId,
        Long            courseId,
        String          courseCourseName,
        String          authorUsername,
        Long            authorId,
        String          title,
        // Raw content — the view uses renderedSegments to build the clickable version
        String          content,
        LocalDateTime   createdAt,
        // Pre-parsed segments: each segment is either plain text or a chapter link
        List<ContentSegment> renderedSegments
) {
    // A segment of the announcement content
    // If chapterId is null → plain text; if not null → clickable chapter link
    public record ContentSegment(
            String text,
            Long   chapterId,   // null for plain text
            Long   courseId     // null for plain text — needed to build the href
    ) {}
}