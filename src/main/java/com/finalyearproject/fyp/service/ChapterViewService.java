package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.ChapterResourcesDTO;
import com.finalyearproject.fyp.entity.Chapter;

import java.util.List;

public interface ChapterViewService {
    List<Chapter>       getChaptersForCourse(Long courseId);
    ChapterResourcesDTO getChapterResources(Long courseId, Long chapterId, String userEmail);
}