package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserCourseTestQuestionAttemptId implements Serializable {

    private Long attemptId;
    private Long questionId;
    private Long testId;
    private Long courseId;
    private Long userId;

    public UserCourseTestQuestionAttemptId() {}

    public UserCourseTestQuestionAttemptId(Long attemptId, Long questionId,
                                           Long testId, Long courseId, Long userId) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.testId = testId;
        this.courseId = courseId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseTestQuestionAttemptId)) return false;
        UserCourseTestQuestionAttemptId that = (UserCourseTestQuestionAttemptId) o;
        return Objects.equals(attemptId, that.attemptId) &&
                Objects.equals(questionId, that.questionId) &&
                Objects.equals(testId, that.testId) &&
                Objects.equals(courseId, that.courseId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attemptId, questionId, testId, courseId, userId);
    }
}