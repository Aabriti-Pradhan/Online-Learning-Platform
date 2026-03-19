package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Chapter;
import com.finalyearproject.fyp.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByCourseOrderByChapterOrderAsc(Course course);
    int countByCourse(Course course);
}
