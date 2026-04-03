package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class PostUserMentionId implements Serializable {

    private Long postId;
    private Long mentionedUserId;

    public PostUserMentionId() {}

    public PostUserMentionId(Long postId, Long mentionedUserId) {
        this.postId          = postId;
        this.mentionedUserId = mentionedUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostUserMentionId)) return false;
        PostUserMentionId that = (PostUserMentionId) o;
        return Objects.equals(postId, that.postId) &&
                Objects.equals(mentionedUserId, that.mentionedUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, mentionedUserId);
    }
}