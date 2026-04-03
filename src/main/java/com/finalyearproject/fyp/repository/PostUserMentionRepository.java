package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.PostUserMention;
import com.finalyearproject.fyp.entity.PostUserMentionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostUserMentionRepository extends JpaRepository<PostUserMention, PostUserMentionId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM PostUserMention m WHERE m.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}