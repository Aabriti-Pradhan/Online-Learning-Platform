package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "user_course_resource")
@IdClass(UserCourseResourceId.class)
public class UserCourseResource {

    @Id
    private Long resourceId;

    @Id
    private Long courseId;

    @Id
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "resourceId", insertable = false, updatable = false)
    private Resource resource;

    @ManyToOne
    @JoinColumn(name = "courseId", insertable = false, updatable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

}
