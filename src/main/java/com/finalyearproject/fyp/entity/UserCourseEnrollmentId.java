package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserCourseEnrollmentId implements Serializable {
    private Long enrollmentId;
    private Long courseId;
    private Long userId;

    public UserCourseEnrollmentId() {}

    public UserCourseEnrollmentId(Long enrollmentId, Long courseId, Long userId) {
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof UserCourseEnrollmentId)) return false;
        UserCourseEnrollmentId that = (UserCourseEnrollmentId) o;
        return Objects.equals(enrollmentId, that.enrollmentId) &&
                Objects.equals(courseId, that.courseId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId, courseId, userId);
    }
}