package com.finalyearproject.fyp.dto;

import java.time.LocalDateTime;
import java.util.List;

// Full post with nested replies (used in thread detail view)
public record PostDetailDTO(
        Long postId,
        String title,  // null for replies
        String content,
        String authorUsername,
        Long authorId,
        LocalDateTime createdAt,
        boolean isDeleted,
        List<String> mentionedUsernames,
        List<String> mentionedCourseNames,
        List<PostDetailDTO> replies      // recursive
) {}