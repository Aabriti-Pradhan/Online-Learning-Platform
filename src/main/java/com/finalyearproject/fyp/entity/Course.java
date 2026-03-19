package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter

@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    private String courseName;
    private String courseDesc;
    private LocalDateTime createdAt;
    private String driveFolderId;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("chapterOrder ASC")
    private List<Chapter> chapters;

    @OneToMany(mappedBy = "course")
    private List<UserCourse> userCourses;

    @OneToMany(mappedBy = "course")
    private List<UserCourseEnrollment> enrollments;

    @OneToMany(mappedBy = "course")
    private List<UserCourseResource> resources;

    @OneToMany(mappedBy = "course")
    private List<UserCourseTest> tests;

    @OneToMany(mappedBy = "course")
    private List<UserCourseTestQuestion> questions;

}