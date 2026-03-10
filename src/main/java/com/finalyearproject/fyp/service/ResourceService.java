package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {

    /** Save an uploaded PDF to disk and record it in the DB. Returns the saved Resource. */
    Resource savePdf(Long userId, Long courseId, MultipartFile file) throws Exception;

    /** Save a note (EditorJS JSON) to disk and record it in the DB. Returns the saved Resource. */
    Resource saveNote(Long userId, Long courseId, MultipartFile file) throws Exception;

    /** Returns all resources belonging to a user (across all courses). */
    List<Resource> getUserResources(String email);

    /** Returns all resources for a specific course. */
    List<Resource> getCourseResources(Long courseId);

    /** Delete a resource by ID — removes from disk and DB. */
    void deleteResource(Long resourceId) throws Exception;
}
