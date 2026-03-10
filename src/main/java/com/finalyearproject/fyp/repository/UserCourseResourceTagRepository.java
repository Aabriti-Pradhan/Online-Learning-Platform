package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Resource;
import com.finalyearproject.fyp.entity.UserCourseResourceTag;
import com.finalyearproject.fyp.entity.UserCourseResourceTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCourseResourceTagRepository
        extends JpaRepository<UserCourseResourceTag, UserCourseResourceTagId> {

    @Query("SELECT ucrt FROM UserCourseResourceTag ucrt WHERE ucrt.resource = :resource")
    List<UserCourseResourceTag> findByResource(@Param("resource") Resource resource);

    @Query("SELECT ucrt FROM UserCourseResourceTag ucrt WHERE ucrt.tagId = :tagId")
    List<UserCourseResourceTag> findByTagId(@Param("tagId") Long tagId);
}