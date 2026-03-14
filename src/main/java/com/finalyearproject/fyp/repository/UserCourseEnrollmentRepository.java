package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserCourseEnrollment;
import com.finalyearproject.fyp.entity.UserCourseEnrollmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseEnrollmentRepository
        extends JpaRepository<UserCourseEnrollment, UserCourseEnrollmentId> {

    @Query("SELECT uce FROM UserCourseEnrollment uce WHERE uce.user = :user")
    List<UserCourseEnrollment> findByUser(@Param("user") User user);

    @Query("SELECT uce FROM UserCourseEnrollment uce WHERE uce.course = :course")
    List<UserCourseEnrollment> findByCourse(@Param("course") Course course);

    @Query("SELECT uce FROM UserCourseEnrollment uce WHERE uce.user = :user AND uce.course = :course")
    Optional<UserCourseEnrollment> findByUserAndCourse(@Param("user") User user, @Param("course") Course course);
}