package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {}