package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.CreatePostRequest;
import com.finalyearproject.fyp.dto.PostDetailDTO;
import com.finalyearproject.fyp.dto.ThreadSummaryDTO;
import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.CourseRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.DiscussionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;
    private final UserRepository    userRepository;
    private final CourseRepository  courseRepository;

    // Discussion feed (public — no login required to view)

    @GetMapping("/discussion")
    public String discussionPage(@RequestParam(required = false) String search,
                                 Model model,
                                 HttpServletRequest request,
                                 Authentication auth) {

        List<ThreadSummaryDTO> threads = (search != null && !search.isBlank())
                ? discussionService.searchThreads(search)
                : discussionService.getAllThreads();

        model.addAttribute("threads",     threads);
        model.addAttribute("search",      search);
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("isLoggedIn",  auth != null && auth.isAuthenticated());
        return "discussion/index";
    }

    // Thread detail page (public — no login required to view)

    @GetMapping("/discussion/{postId}")
    public String threadDetail(@PathVariable Long postId,
                               Model model,
                               HttpServletRequest request,
                               Authentication auth) {

        PostDetailDTO thread = discussionService.getThreadDetail(postId);

        boolean isLoggedIn   = auth != null && auth.isAuthenticated();
        Long    currentUserId = null;

        // Resolve the logged-in user's ID so the template can show delete
        // only on posts that belong to the current user
        if (isLoggedIn) {
            String email = YourCoursesController.extractEmail(auth);
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                currentUserId = userOpt.get().getUserId();
            }
        }

        List<Course> allCourses = courseRepository.findAll();
        List<User>   allUsers   = userRepository.findAll();

        model.addAttribute("thread",        thread);
        model.addAttribute("allUsers",      allUsers);
        model.addAttribute("allCourses",    allCourses);
        model.addAttribute("currentPath",   request.getRequestURI());
        model.addAttribute("isLoggedIn",    isLoggedIn);
        model.addAttribute("currentUserId", currentUserId);   // ← NEW
        return "discussion/thread";
    }

    // Create post or reply (login required)

    @PostMapping("/discussion/post")
    @ResponseBody
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request,
                                        Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "You must be logged in to post."));
        }
        try {
            String        email  = YourCoursesController.extractEmail(auth);
            PostDetailDTO result = discussionService.createPost(email, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Delete post (login required, author only)

    @DeleteMapping("/discussion/post/{postId}")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable Long postId,
                                        Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "You must be logged in."));
        }
        try {
            String email = YourCoursesController.extractEmail(auth);
            boolean isThread = discussionService.deletePost(postId, email);
            return ResponseEntity.ok(Map.of(
                    "message", "Post deleted.",
                    "isThread", isThread
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // Autocomplete: search users by username

    @GetMapping("/discussion/search/users")
    @ResponseBody
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        List<Map<String, Object>> results = userRepository.findAll().stream()
                .filter(u -> u.getUsername() != null &&
                        u.getUsername().toLowerCase().contains(q.toLowerCase()))
                .limit(8)
                .map(u -> Map.<String, Object>of(
                        "id",       u.getUserId(),
                        "username", u.getUsername()
                ))
                .toList();
        return ResponseEntity.ok(results);
    }

    // Autocomplete: search courses by name

    @GetMapping("/discussion/search/courses")
    @ResponseBody
    public ResponseEntity<?> searchCourses(@RequestParam String q) {
        List<Map<String, Object>> results = courseRepository.findAll().stream()
                .filter(c -> c.getCourseName() != null &&
                        c.getCourseName().toLowerCase().contains(q.toLowerCase()))
                .limit(8)
                .map(c -> Map.<String, Object>of(
                        "id",   c.getCourseId(),
                        "name", c.getCourseName()
                ))
                .toList();
        return ResponseEntity.ok(results);
    }
}