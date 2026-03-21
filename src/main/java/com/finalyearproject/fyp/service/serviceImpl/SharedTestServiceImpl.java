package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.ShareTestRequest;
import com.finalyearproject.fyp.dto.SharedTestDTO;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.NotificationService;
import com.finalyearproject.fyp.service.SharedTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SharedTestServiceImpl implements SharedTestService {

    private final SharedTestRepository            sharedTestRepository;
    private final TestRepository                  testRepository;
    private final CourseRepository                courseRepository;
    private final UserRepository                  userRepository;
    private final UserCourseTestRepository        uctRepository;
    private final UserCourseTestQuestionRepository uctqRepository;
    private final NotificationService             notificationService;

    //  Share a test with one or more friends

    @Override
    @Transactional
    public void shareTest(String senderEmail, ShareTestRequest request) {
        User   sender = getUser(senderEmail);
        Test   test   = getTest(request.testId());
        Course course = getCourse(request.courseId());

        for (Long friendId : request.friendIds()) {
            User friend = userRepository.findById(friendId)
                    .orElseThrow(() -> new RuntimeException("Friend not found: " + friendId));

            // Skip teachers — tests can only be shared between students
            if ("TEACHER".equals(friend.getRole())) continue;

            // Skip if already shared
            if (sharedTestRepository.findByTestAndSharedByAndSharedTo(test, sender, friend).isPresent()) {
                continue;
            }

            SharedTest st = new SharedTest();
            st.setTest(test);
            st.setCourse(course);
            st.setSharedBy(sender);
            st.setSharedTo(friend);
            st.setSharedAt(LocalDateTime.now());
            sharedTestRepository.save(st);

            // Notify the recipient
            notificationService.sendToUser(
                    friend.getUserId(),
                    "Test Shared With You",
                    sender.getUsername() + " shared the test \"" + test.getTestTitle() + "\" with you.",
                    "TEST_SHARED",
                    test.getTestId()
            );
        }
    }

    //  Tests shared with me

    @Override
    public List<SharedTestDTO> getTestsSharedWithMe(String userEmail) {
        User user = getUser(userEmail);
        return sharedTestRepository.findBySharedTo(user)
                .stream().map(st -> toDTO(st, st.getSharedBy()))
                .toList();
    }

    //  Tests shared by me

    @Override
    public List<SharedTestDTO> getTestsSharedByMe(String userEmail) {
        User user = getUser(userEmail);
        return sharedTestRepository.findBySharedBy(user)
                .stream().map(st -> toDTO(st, st.getSharedBy()))
                .toList();
    }

    //  Already shared friend IDs

    @Override
    public List<Long> getAlreadySharedFriendIds(Long testId, String senderEmail) {
        User sender = getUser(senderEmail);
        Test test   = getTest(testId);
        return sharedTestRepository.findByTest(test).stream()
                .filter(st -> st.getSharedBy().getUserId().equals(sender.getUserId()))
                .map(st -> st.getSharedTo().getUserId())
                .toList();
    }

    //  helpers

    private SharedTestDTO toDTO(SharedTest st, User sharedBy) {
        Test    test    = st.getTest();
        Course  course  = st.getCourse();
        String  raw     = test.getTestType();
        String  type    = raw != null && raw.contains(":") ? raw.split(":")[0] : raw;
        Integer timer   = null;
        if (raw != null && raw.contains(":")) {
            try { timer = Integer.parseInt(raw.split(":")[1]); } catch (Exception ignored) {}
        }

        // Get chapterId from UserCourseTest
        Long chapterId = uctRepository.findByCourse(course).stream()
                .filter(uct -> uct.getTestId().equals(test.getTestId()))
                .findFirst()
                .map(uct -> uct.getChapter() != null ? uct.getChapter().getChapterId() : null)
                .orElse(null);

        long qCount = uctqRepository.findByTest(test).size();

        return new SharedTestDTO(
                st.getSharedTestId(),
                test.getTestId(),
                test.getTestTitle(),
                type,
                timer,
                course.getCourseId(),
                course.getCourseName(),
                chapterId,
                sharedBy.getUserId(),
                sharedBy.getUsername(),
                st.getSharedAt(),
                qCount
        );
    }

    private User   getUser(String email) { return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email)); }
    private Test   getTest(Long id)      { return testRepository.findById(id).orElseThrow(() -> new RuntimeException("Test not found: " + id)); }
    private Course getCourse(Long id)    { return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found: " + id)); }
}