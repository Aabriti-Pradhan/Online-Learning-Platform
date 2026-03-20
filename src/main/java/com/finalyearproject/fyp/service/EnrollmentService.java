package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.EnrollmentDTO;
import com.finalyearproject.fyp.entity.Course;

import java.util.List;
import java.util.Set;

public interface EnrollmentService {
    EnrollmentDTO enroll(String userEmail, Long courseId);
    void          unenroll(String userEmail, Long courseId);
    Set<Long>     getEnrolledCourseIds(String userEmail);
    List<Course>  getAllCourses();
}