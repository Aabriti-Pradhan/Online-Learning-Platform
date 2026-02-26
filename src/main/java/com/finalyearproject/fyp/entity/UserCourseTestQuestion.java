package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "user_course_test_question")
@IdClass(UserCourseTestQuestionId.class)
public class UserCourseTestQuestion {

    @Id
    private Long questionId;

    @Id
    private Long testId;

    @Id
    private Long courseId;

    @Id
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "questionId", insertable = false, updatable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "testId", insertable = false, updatable = false)
    private Test test;

    @ManyToOne
    @JoinColumn(name = "courseId", insertable = false, updatable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

}