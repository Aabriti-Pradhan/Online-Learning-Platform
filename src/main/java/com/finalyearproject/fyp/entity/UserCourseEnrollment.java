package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "user_course_enrollment")
@IdClass(UserCourseEnrollmentId.class)
public class UserCourseEnrollment {
    @Id
    private Long enrollmentId;
    @Id
    private Long courseId;
    @Id
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "enrollmentId", insertable = false, updatable = false)
    private Enrollment enrollment;

    @ManyToOne
    @JoinColumn(name = "courseId", insertable = false, updatable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

}