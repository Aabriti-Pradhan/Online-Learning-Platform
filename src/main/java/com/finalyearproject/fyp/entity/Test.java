package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter

@Entity
@Table(name = "test")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testId;

    private String testType;
    private String testTitle;
    private LocalDateTime createdAt;
    private boolean isAiGenerated = false;

    @OneToMany(mappedBy = "test")
    private List<UserCourseTest> userCourseTests;

    @OneToMany(mappedBy = "test")
    private List<UserCourseTestQuestion> questions;

}