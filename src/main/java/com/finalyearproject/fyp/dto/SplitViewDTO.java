package com.finalyearproject.fyp.dto;

import java.util.List;

public record SplitViewDTO(
        Long          pdfResourceId,
        String        pdfName,
        String        pdfPath,
        Long          courseId,
        Long          chapterId,
        List<NoteDTO> notes,
        List<TagDTO>  tags
) {}