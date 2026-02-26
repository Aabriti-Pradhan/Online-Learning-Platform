package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "user_course_resource_tag")
@IdClass(UserCourseResourceTagId.class)
public class UserCourseResourceTag {

    @Id
    private Long tagId;

    @Id
    private Long resourceId;

    @Id
    private Long courseId;

    @Id
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "tagId", insertable = false, updatable = false)
    private Tag tag;

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