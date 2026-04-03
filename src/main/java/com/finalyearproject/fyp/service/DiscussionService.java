package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.CreatePostRequest;
import com.finalyearproject.fyp.dto.PostDetailDTO;
import com.finalyearproject.fyp.dto.ThreadSummaryDTO;

import java.util.List;

public interface DiscussionService {

    // Get all top-level threads for the feed
    List<ThreadSummaryDTO> getAllThreads();

    // Search threads by keyword
    List<ThreadSummaryDTO> searchThreads(String keyword);

    // Get a single thread with all its nested replies
    PostDetailDTO getThreadDetail(Long postId);

    // Create a new top-level thread or a reply
    PostDetailDTO createPost(String authorEmail, CreatePostRequest request);

    // Soft-delete a post (only the author can do this)
    void deletePost(Long postId, String requesterEmail);
}