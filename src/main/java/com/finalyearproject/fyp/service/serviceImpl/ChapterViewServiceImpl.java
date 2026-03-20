package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.ChapterResourcesDTO;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.ChapterViewService;
import com.finalyearproject.fyp.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChapterViewServiceImpl implements ChapterViewService {

    private final CourseRepository   courseRepository;
    private final ChapterRepository  chapterRepository;
    private final UserRepository     userRepository;
    private final ResourceService    resourceService;

    @Override
    public List<Chapter> getChaptersForCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        return chapterRepository.findByCourseOrderByChapterOrderAsc(course);
    }

    @Override
    public ChapterResourcesDTO getChapterResources(Long courseId, Long chapterId, String userEmail) {
        User    user      = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        boolean isTeacher = "TEACHER".equals(user.getRole());

        List<Resource> shared;
        List<Resource> mine = new ArrayList<>();

        shared = resourceService.getChapterTeacherResources(chapterId);
        if (!isTeacher) {
            mine = resourceService.getChapterResourcesByUser(chapterId, user.getUserId());
        }

        long pdfCount  = shared.stream().filter(r -> "PDF".equalsIgnoreCase(r.getResourceType())).count();
        long noteCount = shared.stream().filter(r -> "Note".equalsIgnoreCase(r.getResourceType())).count()
                + mine.stream().filter(r -> "Note".equalsIgnoreCase(r.getResourceType())).count();

        return new ChapterResourcesDTO(shared, mine, pdfCount, noteCount, isTeacher);
    }
}