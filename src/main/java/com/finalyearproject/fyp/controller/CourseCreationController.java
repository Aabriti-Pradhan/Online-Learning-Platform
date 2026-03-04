package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserCourse;
import com.finalyearproject.fyp.repository.CourseRepository;
import com.finalyearproject.fyp.repository.UserCourseRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.serviceImpl.CourseDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CourseCreationController {

    private final CourseDriveService courseDriveService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;

    @PostMapping("/create-course")
    @ResponseBody
    @Transactional
    public Map<String, Object> createCourse(
            @RequestParam String courseName,
            @RequestParam String courseDesc,
            Authentication authentication) throws Exception {

        String folderId = courseDriveService.createCourseFolder(courseName);

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        Course course = new Course();
        course.setCourseName(courseName);
        course.setCourseDesc(courseDesc);
        course.setCreatedAt(LocalDateTime.now());
        course.setDriveFolderId(folderId);

        courseRepository.save(course);

        // Get logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create bridge entity
        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setCourse(course);

        userCourseRepository.save(userCourse);

        return Map.of(
                "courseId", course.getCourseId(),
                "courseName", courseName
        );
    }

    @ModelAttribute
    public void addUserCourses(Model model, Authentication authentication) {

        if (authentication != null) {

            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {

                List<UserCourse> userCourses =
                        userCourseRepository.findByUser(user);

                List<Course> courses = userCourses.stream()
                        .map(UserCourse::getCourse)
                        .toList();

                model.addAttribute("userCourses", courses);
            }
        }
    }

    @DeleteMapping("/delete-course/{courseId}")
    @Transactional
    @ResponseBody
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            System.out.println("Deleting course folder ID: " + course.getDriveFolderId());
            if (course.getDriveFolderId() != null) {
                courseDriveService.deleteCourseFolder(course.getDriveFolderId());
            }

            System.out.println("Deleting user-course entries");
            userCourseRepository.deleteByCourse(course);

            System.out.println("Deleting course itself");
            courseRepository.delete(course);

            return ResponseEntity.ok("Deleted successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
