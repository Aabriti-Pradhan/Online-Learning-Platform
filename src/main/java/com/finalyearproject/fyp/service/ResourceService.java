package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {

    /** Save an uploaded PDF to disk and record it in the DB, scoped to a chapter. */
    Resource savePdf(Long userId, Long courseId, Long chapterId, MultipartFile file) throws Exception;

    /** Save a note (EditorJS JSON) to disk and record it in the DB, scoped to a chapter. */
    Resource saveNote(Long userId, Long courseId, Long chapterId, MultipartFile file) throws Exception;

    /** Returns all resources belonging to a user (across all courses). */
    List<Resource> getUserResources(String email);

    /** Returns all resources for a specific course. */
    List<Resource> getCourseResources(Long courseId);

    /** Returns all resources for a specific chapter. */
    List<Resource> getChapterResources(Long chapterId);

    /** Returns chapter resources NOT created by the given user (teacher/shared resources). */
    List<Resource> getChapterResourcesExcludingUser(Long chapterId, Long userId);

    /** Returns chapter resources created BY the given user (student's own notes). */
    List<Resource> getChapterResourcesByUser(Long chapterId, Long userId);

    /** Delete a resource by ID — removes from disk and DB. */
    void deleteResource(Long resourceId) throws Exception;

    List<Resource> getChapterTeacherResources(Long chapterId);
}