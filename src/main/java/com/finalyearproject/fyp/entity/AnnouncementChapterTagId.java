package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnnouncementChapterTagId implements Serializable {

    private Long announcementId;
    private Long chapterId;

    public AnnouncementChapterTagId() {}

    public AnnouncementChapterTagId(Long announcementId, Long chapterId) {
        this.announcementId = announcementId;
        this.chapterId      = chapterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnnouncementChapterTagId)) return false;
        AnnouncementChapterTagId that = (AnnouncementChapterTagId) o;
        return Objects.equals(announcementId, that.announcementId) &&
                Objects.equals(chapterId, that.chapterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(announcementId, chapterId);
    }
}