package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.ChapterDTO;
import com.finalyearproject.fyp.dto.CreateChapterRequest;

public interface ChapterService {
    ChapterDTO createChapter(Long courseId, CreateChapterRequest request);
    ChapterDTO updateChapter(Long chapterId, CreateChapterRequest request);
    void       deleteChapter(Long chapterId);
}