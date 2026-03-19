package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "chapter")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chapterId;

    private String chapterTitle;
    private String chapterDesc;
    private Integer chapterOrder;
    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL)
    private List<UserCourseResource> resources;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL)
    private List<UserCourseTest> tests;
}
