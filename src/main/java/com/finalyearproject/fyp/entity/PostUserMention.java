package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "post_user_mention")
@IdClass(PostUserMentionId.class)
public class PostUserMention {

    @Id
    private Long postId;

    @Id
    private Long mentionedUserId;

    @ManyToOne
    @JoinColumn(name = "postId", insertable = false, updatable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "mentionedUserId", insertable = false, updatable = false)
    private User mentionedUser;
}