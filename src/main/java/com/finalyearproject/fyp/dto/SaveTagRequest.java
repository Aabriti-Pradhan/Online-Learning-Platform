package com.finalyearproject.fyp.dto;

public record SaveTagRequest(
        Long   pdfResourceId,
        Long   courseId,
        String tagType,
        String tagValue,
        String label
) {}