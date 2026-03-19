package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.LocalFileStorageService;
import com.finalyearproject.fyp.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository           resourceRepository;
    private final UserCourseResourceRepository ucrRepository;
    private final UserRepository               userRepository;
    private final CourseRepository             courseRepository;
    private final ChapterRepository            chapterRepository;
    private final LocalFileStorageService      storageService;

    @Override
    @Transactional
    public Resource savePdf(Long userId, Long courseId, Long chapterId, MultipartFile file) throws Exception {
        User    user    = getUser(userId);
        Course  course  = getCourse(courseId);
        Chapter chapter = getChapter(chapterId);
        String  path    = storageService.store(file, user.getUsername(), course.getCourseName());
        return saveResourceRecord(user, course, chapter, file.getOriginalFilename(), "PDF", path);
    }

    @Override
    @Transactional
    public Resource saveNote(Long userId, Long courseId, Long chapterId, MultipartFile file) throws Exception {
        User    user    = getUser(userId);
        Course  course  = getCourse(courseId);
        Chapter chapter = getChapter(chapterId);
        String  path    = storageService.store(file, user.getUsername(), course.getCourseName());
        return saveResourceRecord(user, course, chapter, file.getOriginalFilename(), "Note", path);
    }

    @Override
    public List<Resource> getUserResources(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return ucrRepository.findByUser(user)
                .stream().map(UserCourseResource::getResource).toList();
    }

    @Override
    public List<Resource> getCourseResources(Long courseId) {
        Course course = getCourse(courseId);
        return ucrRepository.findByCourse(course)
                .stream().map(UserCourseResource::getResource).toList();
    }

    @Override
    public List<Resource> getChapterResources(Long chapterId) {
        Chapter chapter = getChapter(chapterId);
        return ucrRepository.findByChapter(chapter)
                .stream().map(UserCourseResource::getResource).toList();
    }

    @Override
    public List<Resource> getChapterResourcesExcludingUser(Long chapterId, Long userId) {
        Chapter chapter = getChapter(chapterId);
        User    user    = getUser(userId);
        return ucrRepository.findByChapterExcludingUser(chapter, user)
                .stream().map(UserCourseResource::getResource).toList();
    }

    @Override
    public List<Resource> getChapterResourcesByUser(Long chapterId, Long userId) {
        Chapter chapter = getChapter(chapterId);
        User    user    = getUser(userId);
        return ucrRepository.findByChapterAndUser(chapter, user)
                .stream().map(UserCourseResource::getResource).toList();
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId) throws Exception {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found: " + resourceId));
        storageService.delete(resource.getResourcePath());
        ucrRepository.deleteByResource(resource);
        resourceRepository.delete(resource);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Resource saveResourceRecord(User user, Course course, Chapter chapter,
                                        String originalName, String type,
                                        String relativePath) {
        Resource resource = new Resource();
        resource.setResourceName(originalName);
        resource.setResourceType(type);
        resource.setResourcePath(relativePath);
        resource.setUploadedAt(LocalDateTime.now());
        resourceRepository.save(resource);

        UserCourseResource ucr = new UserCourseResource();
        ucr.setId(new UserCourseResourceId(resource.getResourceId(), course.getCourseId(), user.getUserId()));
        ucr.setResource(resource);
        ucr.setUser(user);
        ucr.setCourse(course);
        ucr.setChapter(chapter);
        ucrRepository.save(ucr);

        return resource;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
    }

    private Chapter getChapter(Long chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + chapterId));
    }

    @Override
    public List<Resource> getChapterTeacherResources(Long chapterId) {
        Chapter chapter = getChapter(chapterId);
        return ucrRepository.findTeacherResourcesByChapter(chapter)
                .stream().map(UserCourseResource::getResource).toList();
    }
}