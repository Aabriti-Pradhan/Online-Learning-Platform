package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserCourseTestQuestionRepository
        extends JpaRepository<UserCourseTestQuestion, UserCourseTestQuestionId> {

    @Query("SELECT uctq FROM UserCourseTestQuestion uctq WHERE uctq.test = :test")
    List<UserCourseTestQuestion> findByTest(@Param("test") Test test);

    @Query("SELECT uctq FROM UserCourseTestQuestion uctq WHERE uctq.course = :course")
    List<UserCourseTestQuestion> findByCourse(@Param("course") Course course);

    // Used by unenroll — scoped to one student in one course
    @Query("SELECT uctq FROM UserCourseTestQuestion uctq WHERE uctq.course = :course AND uctq.user = :user")
    List<UserCourseTestQuestion> findByCourseAndUser(@Param("course") Course course, @Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCourseTestQuestion uctq WHERE uctq.course = :course")
    void deleteByCourse(@Param("course") Course course);
}