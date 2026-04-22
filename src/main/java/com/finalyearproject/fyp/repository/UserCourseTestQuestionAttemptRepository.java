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
public interface UserCourseTestQuestionAttemptRepository
        extends JpaRepository<UserCourseTestQuestionAttempt, UserCourseTestQuestionAttemptId> {

    @Query("SELECT ucta FROM UserCourseTestQuestionAttempt ucta WHERE ucta.attempt = :attempt")
    List<UserCourseTestQuestionAttempt> findByAttempt(@Param("attempt") Attempt attempt);

    @Query("SELECT ucta FROM UserCourseTestQuestionAttempt ucta WHERE ucta.test = :test AND ucta.user = :user")
    List<UserCourseTestQuestionAttempt> findByTestAndUser(@Param("test") Test test, @Param("user") User user);

    @Query("SELECT u FROM UserCourseTestQuestionAttempt u WHERE u.user = :user")
    List<UserCourseTestQuestionAttempt> findByUser(@Param("user") User user);

    @Query("SELECT u FROM UserCourseTestQuestionAttempt u WHERE u.attemptId = :attemptId")
    List<UserCourseTestQuestionAttempt> findByAttemptId(@Param("attemptId") Long attemptId);

    @Query("SELECT ucta FROM UserCourseTestQuestionAttempt ucta WHERE ucta.test = :test")
    List<UserCourseTestQuestionAttempt> findByTest(@Param("test") Test test);

    @Query("SELECT ucta FROM UserCourseTestQuestionAttempt ucta WHERE ucta.course = :course")
    List<UserCourseTestQuestionAttempt> findByCourse(@Param("course") Course course);

    // Used by unenroll — scoped to one student in one course
    @Query("SELECT ucta FROM UserCourseTestQuestionAttempt ucta WHERE ucta.course = :course AND ucta.user = :user")
    List<UserCourseTestQuestionAttempt> findByCourseAndUser(@Param("course") Course course, @Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCourseTestQuestionAttempt ucta WHERE ucta.course = :course")
    void deleteByCourse(@Param("course") Course course);
}