package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.CourseRepository;
import com.finalyearproject.fyp.repository.ResourceRepository;
import com.finalyearproject.fyp.repository.UserCourseResourceRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.ResourceService;
import com.google.api.services.drive.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserCourseResourceRepository userCourseResourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    File multipartFile = new File();

    public void savePdf(Long userId, Long courseId,
                        String fileId,
                        String originalFileName) {

        Resource resource = new Resource();
        resource.setResourceName(originalFileName);
        resource.setResourceType("PDF");
        resource.setResourcePath(fileId);
        resource.setUploadedAt(LocalDateTime.now());

        resourceRepository.save(resource);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserCourseResource ucr = new UserCourseResource();
        ucr.setId(new UserCourseResourceId(
                resource.getResourceId(),
                courseId,
                userId
        ));

        ucr.setResource(resource);
        ucr.setUser(user);
        ucr.setCourse(course);

        userCourseResourceRepository.save(ucr);
    }

    public List<Resource> getUserResources(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserCourseResource> userResources =
                userCourseResourceRepository.findByUser(user);

        return userResources.stream()
                .map(UserCourseResource::getResource)
                .toList();
    }
}