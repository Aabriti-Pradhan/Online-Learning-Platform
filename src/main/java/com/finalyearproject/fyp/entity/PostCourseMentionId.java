package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class PostCourseMentionId implements Serializable {

    private Long postId;
    private Long courseId;

    public PostCourseMentionId() {}

    public PostCourseMentionId(Long postId, Long courseId) {
        this.postId    = postId;
        this.courseId  = courseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostCourseMentionId)) return false;
        PostCourseMentionId that = (PostCourseMentionId) o;
        return Objects.equals(postId, that.postId) &&
                Objects.equals(courseId, that.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, courseId);
    }
}