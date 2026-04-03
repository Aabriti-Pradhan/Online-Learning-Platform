package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // NULL means this is a top-level thread; non-null means it's a reply
    @ManyToOne
    @JoinColumn(name = "parent_post_id")
    private Post parentPost;

    // Only top-level posts have a title; replies leave this null
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Soft-delete so nested replies stay intact even if a parent is "deleted"
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    // Replies to this post
    @OneToMany(mappedBy = "parentPost", cascade = CascadeType.ALL)
    private List<Post> replies;

    // User mentions in this post
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostUserMention> userMentions;

    // Course mentions in this post
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostCourseMention> courseMentions;
}