package com.finalyearproject.fyp.dto;

import java.util.List;

// Incoming request when creating a thread or reply
public record CreatePostRequest(
        String title,   // required for top-level threads, null for replies
        String content,                // required always
        Long parentPostId,           // null for top-level threads
        List<Long> mentionedUserIds,       // @user mentions
        List<Long> mentionedCourseIds      // #course mentions
) {}