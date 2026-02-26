package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserCourseResourceId implements Serializable {

    private Long resourceId;
    private Long courseId;
    private Long userId;

    public UserCourseResourceId() {}

    public UserCourseResourceId(Long resourceId, Long courseId, Long userId) {
        this.resourceId = resourceId;
        this.courseId = courseId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseResourceId)) return false;
        UserCourseResourceId that = (UserCourseResourceId) o;
        return Objects.equals(resourceId, that.resourceId) &&
                Objects.equals(courseId, that.courseId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, courseId, userId);
    }
}