package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // All top-level threads (not replies), newest first
    @Query("SELECT p FROM Post p WHERE p.parentPost IS NULL ORDER BY p.createdAt DESC")
    List<Post> findAllThreads();

    // Search threads by keyword in title or content
    @Query("SELECT p FROM Post p WHERE p.parentPost IS NULL " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.createdAt DESC")
    List<Post> searchThreads(@Param("keyword") String keyword);

    // All direct replies to a specific post
    @Query("SELECT p FROM Post p WHERE p.parentPost.postId = :parentId ORDER BY p.createdAt ASC")
    List<Post> findRepliesByParentId(@Param("parentId") Long parentId);
}