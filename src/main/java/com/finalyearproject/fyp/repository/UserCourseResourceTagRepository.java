package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Resource;
import com.finalyearproject.fyp.entity.UserCourseResourceTag;
import com.finalyearproject.fyp.entity.UserCourseResourceTagId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserCourseResourceTagRepository
        extends JpaRepository<UserCourseResourceTag, UserCourseResourceTagId> {

    @Query("SELECT ucrt FROM UserCourseResourceTag ucrt WHERE ucrt.resource = :resource")
    List<UserCourseResourceTag> findByResource(@Param("resource") Resource resource);

    @Query("SELECT ucrt FROM UserCourseResourceTag ucrt WHERE ucrt.tagId = :tagId")
    List<UserCourseResourceTag> findByTagId(@Param("tagId") Long tagId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCourseResourceTag t WHERE t.resource.resourceId IN :resourceIds")
    void deleteByResourceIds(@Param("resourceIds") Set<Long> resourceIds);

    // Used by unenroll — only deletes tags belonging to the specific student
    @Modifying
    @Transactional
    @Query("DELETE FROM UserCourseResourceTag t WHERE t.resource.resourceId IN :resourceIds AND t.userId = :userId")
    void deleteByResourceIdsAndUser(@Param("resourceIds") Set<Long> resourceIds, @Param("userId") Long userId);
}