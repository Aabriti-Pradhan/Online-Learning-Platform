package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCourseTestRepository extends JpaRepository<UserCourseTest, UserCourseTestId> {
    @Query("SELECT uct FROM UserCourseTest uct WHERE uct.course = :course")
    List<UserCourseTest> findByCourse(@Param("course") Course course);

    @Query("SELECT uct FROM UserCourseTest uct WHERE uct.course = :course AND uct.user = :user")
    List<UserCourseTest> findByCourseAndUser(@Param("course") Course course, @Param("user") User user);

    @Query("SELECT uct FROM UserCourseTest uct WHERE uct.chapter = :chapter")
    List<UserCourseTest> findByChapter(@Param("chapter") Chapter chapter);
}
