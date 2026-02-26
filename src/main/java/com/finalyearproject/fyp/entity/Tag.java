package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter

@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    private String tagType;
    private String tagValue;
    private String label;

    @OneToMany(mappedBy = "tag")
    private List<UserCourseResourceTag> resourceTags;

}