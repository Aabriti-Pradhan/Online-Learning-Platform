package com.finalyearproject.fyp.dto;

public record ChapterDTO(
        Long   chapterId,
        String chapterTitle,
        String chapterDesc,
        Integer chapterOrder
) {}