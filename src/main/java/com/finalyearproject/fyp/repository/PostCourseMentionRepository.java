package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.PostCourseMention;
import com.finalyearproject.fyp.entity.PostCourseMentionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostCourseMentionRepository extends JpaRepository<PostCourseMention, PostCourseMentionId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM PostCourseMention m WHERE m.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}