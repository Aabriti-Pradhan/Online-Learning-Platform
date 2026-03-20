package com.finalyearproject.fyp.dto;

import com.finalyearproject.fyp.entity.Resource;
import java.util.List;

public record ChapterResourcesDTO(
        List<Resource> sharedResources,
        List<Resource> myResources,
        long           pdfCount,
        long           noteCount,
        boolean        isTeacher
) {}