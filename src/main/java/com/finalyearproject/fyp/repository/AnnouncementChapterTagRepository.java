package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.AnnouncementChapterTag;
import com.finalyearproject.fyp.entity.AnnouncementChapterTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AnnouncementChapterTagRepository
        extends JpaRepository<AnnouncementChapterTag, AnnouncementChapterTagId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM AnnouncementChapterTag t WHERE t.announcementId = :announcementId")
    void deleteByAnnouncementId(@Param("announcementId") Long announcementId);
}