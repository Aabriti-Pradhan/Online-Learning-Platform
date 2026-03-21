package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.CreateTestRequest;
import com.finalyearproject.fyp.dto.QuestionDTO;
import com.finalyearproject.fyp.dto.TestSummaryDTO;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.AiTestService;
import com.finalyearproject.fyp.service.HuggingFaceService;
import com.finalyearproject.fyp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiTestServiceImpl implements AiTestService {

    private final HuggingFaceService             huggingFaceService;
    private final ChapterRepository              chapterRepository;
    private final CourseRepository               courseRepository;
    private final UserRepository                 userRepository;
    private final UserCourseResourceRepository   ucrRepository;
    private final TestRepository                 testRepository;
    private final QuestionRepository             questionRepository;
    private final UserCourseTestRepository       uctRepository;
    private final UserCourseTestQuestionRepository uctqRepository;
    private final UserCourseEnrollmentRepository enrollmentRepository;
    private final UserCourseRepository                userCourseRepository;
    private final NotificationService            notificationService;

    @Override
    @Transactional
    public TestSummaryDTO generateTestFromChapter(Long courseId, Long chapterId,
                                                  String userEmail, String testTitle,
                                                  int questionCount) throws Exception {
        User    user    = getUser(userEmail);
        Course  course  = getCourse(courseId);
        Chapter chapter = getChapter(chapterId);

        // 1. Collect text from all PDFs in this chapter
        List<UserCourseResource> resources = ucrRepository.findByChapter(chapter);
        StringBuilder combinedText = new StringBuilder();

        for (UserCourseResource ucr : resources) {
            Resource r = ucr.getResource();
            if ("PDF".equalsIgnoreCase(r.getResourceType())) {
                try {
                    String text = huggingFaceService.extractPdfText(r.getResourcePath());
                    combinedText.append(text).append("\n\n");
                } catch (Exception ignored) {
                    // skip unreadable PDFs
                }
            }
        }

        if (combinedText.isEmpty()) {
            throw new RuntimeException(
                    "No PDF resources found in this chapter. Please upload at least one PDF first.");
        }

        // 2. Generate questions via AI
        List<HuggingFaceService.GeneratedQuestion> generated =
                huggingFaceService.generateQuestionsFromText(
                        combinedText.toString(), questionCount);

        if (generated.isEmpty()) {
            throw new RuntimeException(
                    "AI could not generate questions from the provided content. "
                            + "Please try again or upload more detailed PDF resources.");
        }

        // 3. Save as a Test — exactly same flow as manual creation
        Test test = new Test();
        test.setTestTitle(testTitle != null && !testTitle.isBlank()
                ? testTitle : "AI Generated Test — " + chapter.getChapterTitle());
        test.setTestType("QUIZ");
        test.setCreatedAt(LocalDateTime.now());
        test.setAiGenerated(true);
        testRepository.save(test);

        UserCourseTest uct = new UserCourseTest();
        uct.setTestId(test.getTestId());
        uct.setCourseId(course.getCourseId());
        uct.setUserId(user.getUserId());
        uct.setChapter(chapter);
        uctRepository.save(uct);

        for (HuggingFaceService.GeneratedQuestion gq : generated) {
            Question question = new Question();
            question.setQuestionText(gq.questionText());
            question.setOptionA(gq.optionA());
            question.setOptionB(gq.optionB());
            question.setOptionC(gq.optionC());
            question.setOptionD(gq.optionD());
            question.setCorrectAns(gq.correctAns());
            questionRepository.save(question);

            UserCourseTestQuestion uctq = new UserCourseTestQuestion();
            uctq.setQuestionId(question.getQuestionId());
            uctq.setTestId(test.getTestId());
            uctq.setCourseId(course.getCourseId());
            uctq.setUserId(user.getUserId());
            uctqRepository.save(uctq);
        }

        // 4. Notify all enrolled students except the creator + the teacher if student generated
        boolean isTeacher = "TEACHER".equals(user.getRole());

        // Get all enrolled students except the creator
        List<Long> studentsToNotify = enrollmentRepository.findByCourse(course)
                .stream()
                .map(uce -> uce.getUser().getUserId())
                .filter(id -> !id.equals(user.getUserId()))
                .toList();

        if (!studentsToNotify.isEmpty()) {
            notificationService.sendToUsers(
                    studentsToNotify,
                    "New AI Test Available",
                    (isTeacher ? user.getUsername() : "A student") + " created an AI test [" + test.getTestTitle() + "] in " + course.getCourseName(),
                    "NEW_TEST",
                    chapterId
            );
        }

        // If a student generated it, also notify the teacher
        if (!isTeacher) {
            List<Long> teacherIds = userCourseRepository.findByCourse(course)
                    .stream().map(uc -> uc.getUser().getUserId()).toList();
            if (!teacherIds.isEmpty()) {
                notificationService.sendToUsers(
                        teacherIds,
                        "Student Generated AI Test",
                        user.getUsername() + " generated an AI test [" + test.getTestTitle() + "] in " + course.getCourseName(),
                        "NEW_TEST",
                        chapterId
                );
            }
        }

        return new TestSummaryDTO(
                test.getTestId(),
                test.getTestTitle(),
                "QUIZ",
                test.getCreatedAt(),
                generated.size(),
                0,
                true,
                user.getUsername(),
                user.getRole()
        );
    }

    private User    getUser(String e)   { return userRepository.findByEmail(e).orElseThrow(() -> new RuntimeException("User not found")); }
    private Course  getCourse(Long id)  { return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found")); }
    private Chapter getChapter(Long id) { return chapterRepository.findById(id).orElseThrow(() -> new RuntimeException("Chapter not found")); }
}