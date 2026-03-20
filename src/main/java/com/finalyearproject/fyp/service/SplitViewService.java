package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.SaveTagRequest;
import com.finalyearproject.fyp.dto.SplitViewDTO;
import com.finalyearproject.fyp.dto.TagDTO;

public interface SplitViewService {
    SplitViewDTO getSplitViewData(Long pdfResourceId, Long courseId, Long chapterId);
    TagDTO       saveTag(SaveTagRequest request, String userEmail);
    void         deleteTag(Long tagId);
}