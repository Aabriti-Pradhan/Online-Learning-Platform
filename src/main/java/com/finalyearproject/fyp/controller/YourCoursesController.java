package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class YourCoursesController {

    private final CourseRepository     courseRepository;
    private final UserRepository       userRepository;
    private final UserCourseRepository userCourseRepository;
    private final ResourceService      resourceService;
    private final ChapterRepository    chapterRepository;
    private final UserCourseEnrollmentRepository userCourseEnrollmentRepository;

    @GetMapping("/your-courses")
    public String coursesPage(Model model, Authentication authentication) {
        User user = resolveUser(authentication);

        // Courses the user owns (teachers)
        List<Course> owned = userCourseRepository.findByUser(user)
                .stream().map(UserCourse::getCourse).toList();

        List<Course> enrolled = userCourseEnrollmentRepository.findByUser(user)
                .stream().map(UserCourseEnrollment::getCourse).toList();

        // Merge both, no duplicates
        List<Course> all = new java.util.ArrayList<>(owned);
        enrolled.forEach(c -> {
            if (all.stream().noneMatch(o -> o.getCourseId().equals(c.getCourseId())))
                all.add(c);
        });

        model.addAttribute("courses",     all);
        model.addAttribute("currentPath", "/your-courses");
        return "yourCourses/index";
    }

    /** Chapter list for a course — the new landing page when a course card is clicked */
    @GetMapping("/your-courses/{courseId}/chapters")
    public String chaptersPage(@PathVariable Long courseId,
                               Model model,
                               HttpServletRequest request) {
        Course course = getCourse(courseId);
        List<Chapter> chapters = chapterRepository.findByCourseOrderByChapterOrderAsc(course);

        model.addAttribute("course",      course);
        model.addAttribute("chapters",    chapters);
        model.addAttribute("currentPath", request.getRequestURI());
        return "courseChapters/index";
    }

    /** Resources page, now scoped to a chapter */
    @GetMapping("/your-courses/{courseId}/chapters/{chapterId}/resources")
    public String chapterDashboard(@PathVariable Long courseId,
                                   @PathVariable Long chapterId,
                                   Model model,
                                   HttpServletRequest request,
                                   Authentication authentication) {

        Course  course  = getCourse(courseId);
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        User    user       = resolveUser(authentication);
        boolean isTeacher  = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        List<Resource> sharedResources;
        List<Resource> myResources = new java.util.ArrayList<>();

        if (isTeacher) {
            sharedResources = resourceService.getChapterTeacherResources(chapterId);
        } else {
            sharedResources = resourceService.getChapterTeacherResources(chapterId); // students see teacher resources
            myResources     = resourceService.getChapterResourcesByUser(chapterId, user.getUserId()); // + their own
        }

        long pdfCount  = sharedResources.stream().filter(r -> "PDF" .equalsIgnoreCase(r.getResourceType())).count();
        long noteCount = sharedResources.stream().filter(r -> "Note".equalsIgnoreCase(r.getResourceType())).count()
                + myResources.stream().filter(r -> "Note".equalsIgnoreCase(r.getResourceType())).count();

        model.addAttribute("course",          course);
        model.addAttribute("chapter",         chapter);
        model.addAttribute("resources",       sharedResources);
        model.addAttribute("myResources",     myResources);
        model.addAttribute("pdfCount",        pdfCount);
        model.addAttribute("noteCount",       noteCount);
        model.addAttribute("isTeacher",       isTeacher);
        model.addAttribute("currentPath",     request.getRequestURI());
        return "courseResources/index";
    }

    // ── helper — works for both OAuth2 (Google) and form login ───────────────

    private User resolveUser(Authentication authentication) {
        String email = extractEmail(authentication);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));
    }

    public static String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }
        if (principal instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("email");
        }
        return authentication.getName();
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }
}