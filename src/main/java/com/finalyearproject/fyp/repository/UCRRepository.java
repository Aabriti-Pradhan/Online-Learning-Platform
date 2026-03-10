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
public interface UCRRepository extends JpaRepository<UserCourseResource, UserCourseResourceId> {

    // Use explicit JPQL for all queries — Spring Data cannot derive queries from
    // field names when the entity uses an @EmbeddedId composite key.

    @Query("SELECT ucr FROM UserCourseResource ucr WHERE ucr.user = :user")
    List<UserCourseResource> findByUser(@Param("user") User user);

    @Query("SELECT ucr FROM UserCourseResource ucr WHERE ucr.course = :course")
    List<UserCourseResource> findByCourse(@Param("course") Course course);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCourseResource ucr WHERE ucr.resource = :resource")
    void deleteByResource(@Param("resource") Resource resource);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCourseResource ucr WHERE ucr.course = :course")
    void deleteByCourse(@Param("course") Course course);
}