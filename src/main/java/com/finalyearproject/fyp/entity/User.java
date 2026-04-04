package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
    private String email;
    private String password;
    private String role;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;

    private String profilePicture;

    @OneToMany(mappedBy = "user")
    private List<UserCourse> userCourses;

    @OneToMany(mappedBy = "user")
    private List<UserCourseEnrollment> enrollments;

    @OneToMany(mappedBy = "user")
    private List<UserCourseResource> resources;

    @OneToMany(mappedBy = "user")
    private List<UserCourseTest> tests;

    @OneToMany(mappedBy = "user")
    private List<UserCourseTestQuestion> questions;

    @OneToMany(mappedBy = "user")
    private List<UserCourseTestQuestionAttempt> attempts;

    @OneToMany(mappedBy = "user")
    private List<UserPredict> predictions;
}