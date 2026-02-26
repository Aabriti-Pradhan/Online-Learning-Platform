package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserCourseTestQuestionId implements Serializable {

    private Long questionId;
    private Long testId;
    private Long courseId;
    private Long userId;

    public UserCourseTestQuestionId() {}

    public UserCourseTestQuestionId(Long questionId, Long testId, Long courseId, Long userId) {
        this.questionId = questionId;
        this.testId = testId;
        this.courseId = courseId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseTestQuestionId)) return false;
        UserCourseTestQuestionId that = (UserCourseTestQuestionId) o;
        return Objects.equals(questionId, that.questionId) &&
                Objects.equals(testId, that.testId) &&
                Objects.equals(courseId, that.courseId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, testId, courseId, userId);
    }
}