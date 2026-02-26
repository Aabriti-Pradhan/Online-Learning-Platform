package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserCourseResourceTagId implements Serializable {

    private Long tagId;
    private Long resourceId;
    private Long courseId;
    private Long userId;

    public UserCourseResourceTagId() {}

    public UserCourseResourceTagId(Long tagId, Long resourceId, Long courseId, Long userId) {
        this.tagId = tagId;
        this.resourceId = resourceId;
        this.courseId = courseId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseResourceTagId)) return false;
        UserCourseResourceTagId that = (UserCourseResourceTagId) o;
        return Objects.equals(tagId, that.tagId) &&
                Objects.equals(resourceId, that.resourceId) &&
                Objects.equals(courseId, that.courseId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, resourceId, courseId, userId);
    }
}