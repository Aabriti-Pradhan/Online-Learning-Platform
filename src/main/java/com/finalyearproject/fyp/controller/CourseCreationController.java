package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.CourseDTO;
import com.finalyearproject.fyp.dto.CreateCourseRequest;
import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.UserCourseEnrollment;
import com.finalyearproject.fyp.entity.UserCourse;
import com.finalyearproject.fyp.repository.UserCourseEnrollmentRepository;
import com.finalyearproject.fyp.repository.UserCourseRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CourseCreationController {

    private final CourseService                  courseService;
    private final UserRepository                 userRepository;
    private final UserCourseRepository           userCourseRepository;
    private final UserCourseEnrollmentRepository userCourseEnrollmentRepository;

    @PostMapping("/create-course")
    @ResponseBody
    public ResponseEntity<CourseDTO> createCourse(@RequestParam String courseName,
                                                  @RequestParam String courseDesc,
                                                  Authentication authentication) {
        String email = YourCoursesController.extractEmail(authentication);
        CourseDTO dto = courseService.createCourse(email, new CreateCourseRequest(courseName, courseDesc));
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/update-course/{courseId}")
    @ResponseBody
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId,
                                          @RequestBody CreateCourseRequest request) {
        courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    @DeleteMapping("/delete-course/{courseId}")
    @ResponseBody
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            courseService.deleteCourse(courseId);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @ModelAttribute
    public void addUserCourses(Model model, Authentication authentication) {
        if (authentication == null) return;

        String email = YourCoursesController.extractEmail(authentication);
        userRepository.findByEmail(email).ifPresent(user -> {
            boolean isStudent = authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_STUDENT"));

            List<Course> courses;
            if (isStudent) {
                courses = userCourseEnrollmentRepository.findByUser(user)
                        .stream().map(UserCourseEnrollment::getCourse).toList();
            } else {
                courses = userCourseRepository.findByUser(user)
                        .stream().map(UserCourse::getCourse).toList();
            }

            model.addAttribute("userCourses", courses);
            model.addAttribute("loggedInUser", user);
        });
    }
}