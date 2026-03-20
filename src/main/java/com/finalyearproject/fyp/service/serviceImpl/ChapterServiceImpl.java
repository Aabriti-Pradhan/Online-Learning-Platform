package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.ChapterDTO;
import com.finalyearproject.fyp.dto.CreateChapterRequest;
import com.finalyearproject.fyp.entity.Chapter;
import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.repository.ChapterRepository;
import com.finalyearproject.fyp.repository.CourseRepository;
import com.finalyearproject.fyp.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final CourseRepository  courseRepository;

    @Override
    @Transactional
    public ChapterDTO createChapter(Long courseId, CreateChapterRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        int order = chapterRepository.countByCourse(course) + 1;

        Chapter chapter = new Chapter();
        chapter.setCourse(course);
        chapter.setChapterTitle(request.chapterTitle() != null ? request.chapterTitle() : "Untitled Chapter");
        chapter.setChapterDesc(request.chapterDesc() != null ? request.chapterDesc() : "");
        chapter.setChapterOrder(order);
        chapter.setCreatedAt(LocalDateTime.now());
        chapterRepository.save(chapter);

        return toDTO(chapter);
    }

    @Override
    @Transactional
    public ChapterDTO updateChapter(Long chapterId, CreateChapterRequest request) {
        Chapter chapter = getChapter(chapterId);
        if (request.chapterTitle() != null) chapter.setChapterTitle(request.chapterTitle());
        if (request.chapterDesc()  != null) chapter.setChapterDesc(request.chapterDesc());
        chapterRepository.save(chapter);
        return toDTO(chapter);
    }

    @Override
    @Transactional
    public void deleteChapter(Long chapterId) {
        chapterRepository.delete(getChapter(chapterId));
    }

    private Chapter getChapter(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + id));
    }

    private ChapterDTO toDTO(Chapter c) {
        return new ChapterDTO(c.getChapterId(), c.getChapterTitle(), c.getChapterDesc(), c.getChapterOrder());
    }
}