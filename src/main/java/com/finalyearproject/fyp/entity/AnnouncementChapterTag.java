package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "announcement_chapter_tag")
@IdClass(AnnouncementChapterTagId.class)
public class AnnouncementChapterTag {

    @Id
    private Long announcementId;

    @Id
    private Long chapterId;

    @ManyToOne
    @JoinColumn(name = "announcementId", insertable = false, updatable = false)
    private Announcement announcement;

    @ManyToOne
    @JoinColumn(name = "chapterId", insertable = false, updatable = false)
    private Chapter chapter;
}