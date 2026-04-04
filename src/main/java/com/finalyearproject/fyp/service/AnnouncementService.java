package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.AnnouncementDTO;
import com.finalyearproject.fyp.dto.CreateAnnouncementRequest;

import java.util.List;

public interface AnnouncementService {

    // Get all announcements for a course
    List<AnnouncementDTO> getAnnouncementsForCourse(Long courseId);

    // Teacher creates an announcement for their course
    AnnouncementDTO createAnnouncement(Long courseId, String teacherEmail, CreateAnnouncementRequest request);

    // Teacher deletes their own announcement
    void deleteAnnouncement(Long announcementId, String teacherEmail);
}