package com.finalyearproject.fyp.dto;

import java.time.LocalDateTime;

public record UserResourceDTO(
        Long          resourceId,
        String        resourceName,
        String        resourceType,
        String        resourcePath,
        LocalDateTime uploadedAt,
        String        courseName,
        String        chapterTitle
) {}