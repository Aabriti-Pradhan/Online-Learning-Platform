package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.CourseDTO;
import com.finalyearproject.fyp.dto.CreateCourseRequest;
import com.finalyearproject.fyp.entity.Course;

import java.util.List;

public interface CourseService {
    CourseDTO createCourse(String userEmail, CreateCourseRequest request);
    CourseDTO updateCourse(Long courseId, CreateCourseRequest request);
    void deleteCourse(Long courseId);
    List<Course> getCoursesForUser(String userEmail);
}