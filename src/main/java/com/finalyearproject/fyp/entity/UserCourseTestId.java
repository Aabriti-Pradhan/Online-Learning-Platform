package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserCourseTestId implements Serializable {

    private Long testId;
    private Long courseId;
    private Long userId;

    public UserCourseTestId() {}

    public UserCourseTestId(Long testId, Long courseId, Long userId) {
        this.testId = testId;
        this.courseId = courseId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseTestId)) return false;
        UserCourseTestId that = (UserCourseTestId) o;
        return Objects.equals(testId, that.testId) &&
                Objects.equals(courseId, that.courseId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testId, courseId, userId);
    }
}