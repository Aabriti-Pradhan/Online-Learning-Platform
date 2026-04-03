package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.CreatePostRequest;
import com.finalyearproject.fyp.dto.PostDetailDTO;
import com.finalyearproject.fyp.dto.ThreadSummaryDTO;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.DiscussionService;
import com.finalyearproject.fyp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscussionServiceImpl implements DiscussionService {

    private final PostRepository              postRepository;
    private final PostUserMentionRepository   postUserMentionRepository;
    private final PostCourseMentionRepository postCourseMentionRepository;
    private final UserRepository              userRepository;
    private final CourseRepository            courseRepository;
    private final UserCourseRepository        userCourseRepository;
    private final NotificationService         notificationService;

    // All top-level threads

    @Override
    public List<ThreadSummaryDTO> getAllThreads() {
        return postRepository.findAllThreads()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    // Search threads

    @Override
    public List<ThreadSummaryDTO> searchThreads(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllThreads();
        return postRepository.searchThreads(keyword.trim())
                .stream()
                .map(this::toSummary)
                .toList();
    }

    // Thread detail with nested replies

    @Override
    public PostDetailDTO getThreadDetail(Long postId) {
        Post post = getPostById(postId);
        return toDetailDTO(post);
    }

    // Create post (thread or reply)

    @Override
    @Transactional
    public PostDetailDTO createPost(String authorEmail, CreatePostRequest request) {
        User author = getUserByEmail(authorEmail);

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(request.content());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setDeleted(false);

        // Thread vs reply
        if (request.parentPostId() != null) {
            Post parent = getPostById(request.parentPostId());
            post.setParentPost(parent);
            post.setTitle(null);

            // Fix 3: Notify the parent post's author that someone replied
            // Don't notify if the author is replying to their own post
            Long parentAuthorId = parent.getAuthor().getUserId();
            if (!parentAuthorId.equals(author.getUserId())) {

                // Decide the notification message based on whether the parent
                // is a top-level thread or itself a reply
                boolean parentIsThread = parent.getParentPost() == null;
                String notifTitle   = parentIsThread
                        ? "Someone replied to your thread"
                        : "Someone replied to your reply";
                String notifMessage = author.getUsername() + " replied to your "
                        + (parentIsThread ? "thread" : "reply")
                        + (parentIsThread && parent.getTitle() != null
                        ? " \"" + parent.getTitle() + "\""
                        : "")
                        + ".";

                notificationService.sendToUser(
                        parentAuthorId,
                        notifTitle,
                        notifMessage,
                        "DISCUSSION_REPLY",
                        // referenceId points to the ROOT thread so clicking the
                        // notification takes the user to the right thread page
                        getRootPostId(parent)
                );
            }

        } else {
            post.setTitle(request.title());
        }

        postRepository.save(post);

        // User @mentions
        if (request.mentionedUserIds() != null) {
            for (Long mentionedUserId : request.mentionedUserIds()) {
                userRepository.findById(mentionedUserId).ifPresent(mentionedUser -> {
                    PostUserMention mention = new PostUserMention();
                    mention.setPostId(post.getPostId());
                    mention.setMentionedUserId(mentionedUserId);
                    postUserMentionRepository.save(mention);

                    // Don't notify users who mention themselves
                    if (!mentionedUserId.equals(author.getUserId())) {
                        notificationService.sendToUser(
                                mentionedUserId,
                                "You were mentioned in a discussion",
                                author.getUsername() + " mentioned you in a post.",
                                "DISCUSSION_MENTION",
                                post.getPostId()
                        );
                    }
                });
            }
        }

        // Course #mentions
        if (request.mentionedCourseIds() != null) {
            for (Long courseId : request.mentionedCourseIds()) {
                courseRepository.findById(courseId).ifPresent(course -> {
                    PostCourseMention mention = new PostCourseMention();
                    mention.setPostId(post.getPostId());
                    mention.setCourseId(courseId);
                    postCourseMentionRepository.save(mention);

                    // Notify the teacher(s) of the tagged course
                    List<Long> teacherIds = userCourseRepository.findByCourse(course)
                            .stream()
                            .map(uc -> uc.getUser().getUserId())
                            .toList();

                    if (!teacherIds.isEmpty()) {
                        notificationService.sendToUsers(
                                teacherIds,
                                "Your course was mentioned",
                                author.getUsername() + " mentioned your course \""
                                        + course.getCourseName() + "\" in a discussion.",
                                "COURSE_TAGGED",
                                post.getPostId()
                        );
                    }
                });
            }
        }

        return toDetailDTO(post);
    }

    // Soft delete

    @Override
    @Transactional
    public boolean deletePost(Long postId, String requesterEmail) {
        Post post   = getPostById(postId);
        User caller = getUserByEmail(requesterEmail);

        if (!post.getAuthor().getUserId().equals(caller.getUserId())) {
            throw new SecurityException("You can only delete your own posts.");
        }

        if (post.getParentPost() == null) {

            // delete mentions first (avoid FK issues)
            postUserMentionRepository.deleteByPostId(postId);
            postCourseMentionRepository.deleteByPostId(postId);

            // delete the post → cascades to replies automatically
            postRepository.delete(post);
            return true;

        } else {
            post.setDeleted(true);
            post.setContent("[deleted]");
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);
            return false;
        }
    }

    // Private helpers

    // Walk up the parent chain to find the root thread's ID
    // Used so reply notifications deep-link to the root thread page
    private Long getRootPostId(Post post) {
        Post current = post;
        while (current.getParentPost() != null) {
            current = current.getParentPost();
        }
        return current.getPostId();
    }

    private ThreadSummaryDTO toSummary(Post post) {
        int replyCount = postRepository.findRepliesByParentId(post.getPostId()).size();

        List<String> mentionedUsers = post.getUserMentions() == null ? List.of() :
                post.getUserMentions().stream()
                        .map(m -> m.getMentionedUser().getUsername())
                        .toList();

        List<String> mentionedCourses = post.getCourseMentions() == null ? List.of() :
                post.getCourseMentions().stream()
                        .map(m -> m.getCourse().getCourseName())
                        .toList();

        return new ThreadSummaryDTO(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getUsername(),
                post.getAuthor().getUserId(),
                post.getCreatedAt(),
                replyCount,
                mentionedUsers,
                mentionedCourses
        );
    }

    private PostDetailDTO toDetailDTO(Post post) {
        List<String> mentionedUsers = post.getUserMentions() == null ? List.of() :
                post.getUserMentions().stream()
                        .map(m -> m.getMentionedUser().getUsername())
                        .toList();

        List<String> mentionedCourses = post.getCourseMentions() == null ? List.of() :
                post.getCourseMentions().stream()
                        .map(m -> m.getCourse().getCourseName())
                        .toList();

        List<PostDetailDTO> replies = postRepository
                .findRepliesByParentId(post.getPostId())
                .stream()
                .map(this::toDetailDTO)
                .toList();

        return new PostDetailDTO(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getUsername(),
                post.getAuthor().getUserId(),
                post.getCreatedAt(),
                post.isDeleted(),
                mentionedUsers,
                mentionedCourses,
                replies
        );
    }

    private Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}