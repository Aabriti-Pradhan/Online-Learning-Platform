package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Announcement;
import com.finalyearproject.fyp.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // All announcements for a course, newest first
    List<Announcement> findByCourseOrderByCreatedAtDesc(Course course);
}