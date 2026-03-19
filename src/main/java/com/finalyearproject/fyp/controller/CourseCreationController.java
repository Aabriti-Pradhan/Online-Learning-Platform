package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserCourse;
import com.finalyearproject.fyp.entity.UserCourseEnrollment;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.controller.YourCoursesController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Phase 1 — Google Drive removed. Courses are pure DB records now.
 * Local folders are created automatically by LocalFileStorageService on first upload.
 */
@Controller
@RequiredArgsConstructor
public class CourseCreationController {

    private final CourseRepository              courseRepository;
    private final UserRepository                userRepository;
    private final UserCourseRepository          userCourseRepository;
    private final UserCourseResourceRepository  ucrRepository;
    private final UserCourseEnrollmentRepository userCourseEnrollmentRepository;

    // ── Create course ─────────────────────────────────────────────────────────

    @PostMapping("/create-course")
    @ResponseBody
    @Transactional
    public Map<String, Object> createCourse(
            @RequestParam String courseName,
            @RequestParam String courseDesc,
            Authentication authentication) {

        String email = YourCoursesController.extractEmail(authentication);
        User   user  = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = new Course();
        course.setCourseName(courseName);
        course.setCourseDesc(courseDesc);
        course.setCreatedAt(LocalDateTime.now());
        // driveFolderId left null — no longer used
        courseRepository.save(course);

        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setCourse(course);
        userCourseRepository.save(userCourse);

        return Map.of(
                "courseId",   course.getCourseId(),
                "courseName", courseName
        );
    }

    // ── Update course name/description ───────────────────────────────────────

    @PutMapping("/update-course/{courseId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId,
                                          @RequestBody Map<String, String> payload) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (payload.containsKey("courseName")) course.setCourseName(payload.get("courseName"));
        if (payload.containsKey("courseDesc"))  course.setCourseDesc(payload.get("courseDesc"));
        courseRepository.save(course);

        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    // ── Delete course ─────────────────────────────────────────────────────────

    @DeleteMapping("/delete-course/{courseId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Remove join-table rows
            ucrRepository.deleteByCourse(course);
            userCourseRepository.deleteByCourse(course);

            courseRepository.delete(course);
            return ResponseEntity.ok("Deleted successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // ── Global model attribute — injects userCourses into every view ──────────

    @ModelAttribute
    public void addUserCourses(Model model, Authentication authentication) {
        if (authentication == null) return;

        String email = YourCoursesController.extractEmail(authentication);
        userRepository.findByEmail(email).ifPresent(user -> {
            List<Course> owned = userCourseRepository.findByUser(user)
                    .stream().map(UserCourse::getCourse).toList();

            List<Course> enrolled = userCourseEnrollmentRepository.findByUser(user)
                    .stream().map(UserCourseEnrollment::getCourse).toList();

            List<Course> all = new java.util.ArrayList<>(owned);
            enrolled.forEach(c -> {
                if (all.stream().noneMatch(o -> o.getCourseId().equals(c.getCourseId())))
                    all.add(c);
            });

            model.addAttribute("userCourses", all);
            model.addAttribute("loggedInUser", user);
        });
    }
}