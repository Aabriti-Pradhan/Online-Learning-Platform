package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCourseTestQuestionAttemptRepository
        extends JpaRepository<UserCourseTestQuestionAttempt, UserCourseTestQuestionAttemptId> {

    @Query("SELECT ucta FROM UserCourseTestQuestionAttempt ucta WHERE ucta.attempt = :attempt")
    List<UserCourseTestQuestionAttempt> findByAttempt(@Param("attempt") Attempt attempt);

    @Query("SELECT ucta FROM UserCourseTestQuestionAttempt ucta WHERE ucta.test = :test AND ucta.user = :user")
    List<UserCourseTestQuestionAttempt> findByTestAndUser(@Param("test") Test test, @Param("user") User user);

    @Query("SELECT ucta FROM UserCourseTestQuestionAttempt ucta WHERE ucta.test = :test")
    List<UserCourseTestQuestionAttempt> findByTest(@Param("test") Test test);
}