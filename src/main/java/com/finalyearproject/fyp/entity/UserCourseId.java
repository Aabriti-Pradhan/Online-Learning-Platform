package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserCourseId implements Serializable {
    private Long courseId;
    private Long userId;

    public UserCourseId() {}
    public UserCourseId(Long courseId, Long userId) {
        this.courseId = courseId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof UserCourseId)) return false;
        UserCourseId that = (UserCourseId) o;
        return Objects.equals(courseId, that.courseId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, userId);
    }
}
