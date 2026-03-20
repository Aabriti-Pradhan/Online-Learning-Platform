package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.ChapterResourcesDTO;
import com.finalyearproject.fyp.entity.Chapter;
import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.repository.ChapterRepository;
import com.finalyearproject.fyp.repository.CourseRepository;
import com.finalyearproject.fyp.service.ChapterViewService;
import com.finalyearproject.fyp.service.CourseService;
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

    private final CourseService      courseService;
    private final ChapterViewService chapterViewService;
    private final CourseRepository   courseRepository;
    private final ChapterRepository  chapterRepository;

    @GetMapping("/your-courses")
    public String coursesPage(Model model, Authentication authentication) {
        String       email   = extractEmail(authentication);
        List<Course> courses = courseService.getCoursesForUser(email);
        model.addAttribute("courses",     courses);
        model.addAttribute("currentPath", "/your-courses");
        return "yourCourses/index";
    }

    @GetMapping("/your-courses/{courseId}/chapters")
    public String chaptersPage(@PathVariable Long courseId,
                               Model model,
                               HttpServletRequest request) {
        Course         course   = getCourse(courseId);
        List<Chapter>  chapters = chapterViewService.getChaptersForCourse(courseId);
        model.addAttribute("course",      course);
        model.addAttribute("chapters",    chapters);
        model.addAttribute("currentPath", request.getRequestURI());
        return "courseChapters/index";
    }

    @GetMapping("/your-courses/{courseId}/chapters/{chapterId}/resources")
    public String chapterDashboard(@PathVariable Long courseId,
                                   @PathVariable Long chapterId,
                                   Model model,
                                   HttpServletRequest request,
                                   Authentication authentication) {
        String             email   = extractEmail(authentication);
        Course             course  = getCourse(courseId);
        Chapter            chapter = getChapter(chapterId);
        ChapterResourcesDTO data   = chapterViewService.getChapterResources(courseId, chapterId, email);

        model.addAttribute("course",      course);
        model.addAttribute("chapter",     chapter);
        model.addAttribute("resources",   data.sharedResources());
        model.addAttribute("myResources", data.myResources());
        model.addAttribute("pdfCount",    data.pdfCount());
        model.addAttribute("noteCount",   data.noteCount());
        model.addAttribute("isTeacher",   data.isTeacher());
        model.addAttribute("currentPath", request.getRequestURI());
        return "courseResources/index";
    }

    // Static helper — used by other controllers to extract email

    public static String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser  u) return u.getEmail();
        if (principal instanceof OAuth2User u) return u.getAttribute("email");
        return authentication.getName();
    }

    private Course  getCourse(Long id)  { return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found")); }
    private Chapter getChapter(Long id) { return chapterRepository.findById(id).orElseThrow(() -> new RuntimeException("Chapter not found")); }
}