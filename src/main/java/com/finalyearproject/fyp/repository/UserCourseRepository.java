package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {
    List<UserCourse> findByUser(User user);

    List<UserCourse> findByCourse(Course course);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCourse uc WHERE uc.course = :course")
    void deleteByCourse(@Param("course") Course course);
}
