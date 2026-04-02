package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.CourseDTO;
import com.finalyearproject.fyp.dto.CreateCourseRequest;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.CourseService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserCourseEnrollmentRepository enrollmentRepository;
    private final UserCourseResourceRepository ucrRepository;
    private final UserCourseResourceTagRepository ucrTagRepository;
    private final ResourceRepository resourceRepository;
    private final UserCourseTestQuestionAttemptRepository uctqaRepository;
    private final AttemptAnsRepository attemptAnsRepository;
    private final AttemptRepository attemptRepository;
    private final UserCourseTestQuestionRepository uctqRepository;
    private final UserCourseTestRepository uctRepository;
    private final SharedTestRepository sharedTestRepository;
    private final TestRepository testRepository;
    private final EnrollmentRepository enrollmentEntityRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public CourseDTO createCourse(String userEmail, CreateCourseRequest request) {
        User user = getUser(userEmail);

        Course course = new Course();
        course.setCourseName(request.courseName());
        course.setCourseDesc(request.courseDesc());
        course.setCreatedAt(LocalDateTime.now());
        courseRepository.save(course);

        UserCourse uc = new UserCourse();
        uc.setUser(user);
        uc.setCourse(course);
        userCourseRepository.save(uc);

        return toDTO(course);
    }

    @Override
    @Transactional
    public CourseDTO updateCourse(Long courseId, CreateCourseRequest request) {
        Course course = getCourse(courseId);
        if (request.courseName() != null) course.setCourseName(request.courseName());
        if (request.courseDesc()  != null) course.setCourseDesc(request.courseDesc());
        courseRepository.save(course);
        return toDTO(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = getCourse(courseId);

        // Collect attempt IDs before deleting anything
        Set<Long> attemptIds = uctqaRepository.findByCourse(course)
                .stream()
                .map(UserCourseTestQuestionAttempt::getAttemptId)
                .collect(Collectors.toSet());

        // Delete attempt_ans, then UCTQA rows, then attempt entities — all via bulk JPQL
        if (!attemptIds.isEmpty()) {
            attemptAnsRepository.deleteByAttemptIds(attemptIds);
        }
        uctqaRepository.deleteByCourse(course);
        if (!attemptIds.isEmpty()) {
            attemptRepository.deleteByAttemptIds(attemptIds);
        }

        // Collect question IDs before deleting links
        Set<Long> questionIds = uctqRepository.findByCourse(course)
                .stream()
                .map(UserCourseTestQuestion::getQuestionId)
                .collect(Collectors.toSet());

        uctqRepository.deleteByCourse(course);

        if (!questionIds.isEmpty()) {
            questionRepository.deleteByQuestionIds(questionIds);
        }

        // Collect test IDs before deleting links
        Set<Long> testIds = uctRepository.findByCourse(course)
                .stream()
                .map(UserCourseTest::getTestId)
                .collect(Collectors.toSet());

        sharedTestRepository.deleteByCourse(course);
        uctRepository.deleteByCourse(course);

        if (!testIds.isEmpty()) {
            testRepository.deleteByTestIds(testIds);
        }

        // Resources: delete tags first, then UCR links, then resource entities
        List<UserCourseResource> ucrs = ucrRepository.findByCourse(course);
        Set<Long> resourceIds = ucrs.stream()
                .map(ucr -> ucr.getResource().getResourceId())
                .collect(Collectors.toSet());

        if (!resourceIds.isEmpty()) {
            ucrTagRepository.deleteByResourceIds(resourceIds);
        }
        ucrRepository.deleteByCourse(course);
        if (!resourceIds.isEmpty()) {
            resourceRepository.deleteByResourceIds(resourceIds);
        }

        // Enrollments
        Set<Long> enrollmentIds = enrollmentRepository.findByCourse(course)
                .stream()
                .map(UserCourseEnrollment::getEnrollmentId)
                .collect(Collectors.toSet());

        enrollmentRepository.deleteByCourse(course);

        if (!enrollmentIds.isEmpty()) {
            enrollmentEntityRepository.deleteByEnrollmentIds(enrollmentIds);
        }

        userCourseRepository.deleteByCourse(course);

        entityManager.flush();
        entityManager.clear();

        chapterRepository.findByCourseOrderByChapterOrderAsc(course)
                .forEach(chapterRepository::delete);

        courseRepository.delete(course);
    }

    @Override
    public List<Course> getCoursesForUser(String userEmail) {
        User user = getUser(userEmail);

        List<Course> owned = userCourseRepository.findByUser(user)
                .stream().map(UserCourse::getCourse).toList();

        List<Course> enrolled = enrollmentRepository.findByUser(user)
                .stream().map(UserCourseEnrollment::getCourse).toList();

        List<Course> all = new ArrayList<>(owned);
        enrolled.forEach(c -> {
            if (all.stream().noneMatch(o -> o.getCourseId().equals(c.getCourseId())))
                all.add(c);
        });
        return all;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
    }

    private CourseDTO toDTO(Course c) {
        return new CourseDTO(c.getCourseId(), c.getCourseName(), c.getCourseDesc());
    }
}