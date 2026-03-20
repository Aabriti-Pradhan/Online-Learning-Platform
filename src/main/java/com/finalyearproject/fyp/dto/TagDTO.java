package com.finalyearproject.fyp.dto;

public record TagDTO(
        Long   tagId,
        String tagType,
        String tagValue,
        String label
) {}