package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "user_course_resource")
public class UserCourseResource {

    @EmbeddedId
    private UserCourseResourceId id;

    @ManyToOne
    @MapsId("resourceId")
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
}