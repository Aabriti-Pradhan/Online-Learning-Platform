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
public interface UserCourseResourceRepository extends JpaRepository<UserCourseResource, UserCourseResourceId> {

    @Query("SELECT ucr FROM UserCourseResource ucr WHERE ucr.user = :user")
    List<UserCourseResource> findByUser(@Param("user") User user);

    @Query("SELECT ucr FROM UserCourseResource ucr WHERE ucr.course = :course")
    List<UserCourseResource> findByCourse(@Param("course") Course course);

    // Used by unenroll — scoped to one student in one course
    @Query("SELECT ucr FROM UserCourseResource ucr WHERE ucr.user = :user AND ucr.course = :course")
    List<UserCourseResource> findByUserAndCourse(@Param("user") User user, @Param("course") Course course);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCourseResource ucr WHERE ucr.resource = :resource")
    void deleteByResource(@Param("resource") Resource resource);

    @Query("SELECT ucr FROM UserCourseResource ucr WHERE ucr.chapter = :chapter")
    List<UserCourseResource> findByChapter(@Param("chapter") Chapter chapter);

    @Query("SELECT ucr FROM UserCourseResource ucr WHERE ucr.chapter = :chapter AND ucr.user = :user")
    List<UserCourseResource> findByChapterAndUser(@Param("chapter") Chapter chapter, @Param("user") User user);

    @Query("SELECT ucr FROM UserCourseResource ucr WHERE ucr.chapter = :chapter AND ucr.user != :user")
    List<UserCourseResource> findByChapterExcludingUser(@Param("chapter") Chapter chapter, @Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCourseResource ucr WHERE ucr.course = :course")
    void deleteByCourse(@Param("course") Course course);

    @Query("SELECT ucr FROM UserCourseResource ucr WHERE ucr.chapter = :chapter AND ucr.user.role = 'TEACHER'")
    List<UserCourseResource> findTeacherResourcesByChapter(@Param("chapter") Chapter chapter);
}