package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter

@Entity
@Table(name = "resource")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    private String resourceType;
    private String resourcePath;
    private LocalDateTime uploadedAt;
    private String resourceName;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isPrivate = false;

    @OneToMany(mappedBy = "resource")
    private List<UserCourseResource> userCourseResources;

    @OneToMany(mappedBy = "resource")
    private List<UserCourseResourceTag> resourceTags;

}