package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.NoteDTO;

import java.util.Map;

public interface NoteService {
    NoteDTO saveNote(String userEmail, Long courseId, Long chapterId, String title, Object content) throws Exception;
    void    updateNote(Long resourceId, String title, Object content) throws Exception;
    Object  loadNote(Long resourceId) throws Exception;
    String  getNoteTitle(Long resourceId);
    boolean isNoteOwner(Long resourceId, String userEmail);
}