package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.NoteDTO;
import com.finalyearproject.fyp.dto.SaveTagRequest;
import com.finalyearproject.fyp.dto.SplitViewDTO;
import com.finalyearproject.fyp.dto.TagDTO;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.SplitViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SplitViewServiceImpl implements SplitViewService {

    private final ResourceRepository              resourceRepository;
    private final UserRepository                  userRepository;
    private final CourseRepository                courseRepository;
    private final ChapterRepository               chapterRepository;
    private final UserCourseResourceRepository    ucrRepository;
    private final TagRepository                   tagRepository;
    private final UserCourseResourceTagRepository ucrtRepository;

    @Override
    public SplitViewDTO getSplitViewData(Long pdfResourceId, Long courseId, Long chapterId) {
        Resource pdf     = getResource(pdfResourceId);
        Chapter  chapter = getChapter(chapterId);

        List<NoteDTO> notes = ucrRepository.findByChapter(chapter).stream()
                .map(UserCourseResource::getResource)
                .filter(r -> "Note".equalsIgnoreCase(r.getResourceType()))
                .map(r -> new NoteDTO(r.getResourceId(), r.getResourceName(), r.getResourcePath()))
                .toList();

        List<TagDTO> tags = ucrtRepository.findByResource(pdf).stream()
                .map(ucrt -> new TagDTO(
                        ucrt.getTag().getTagId(),
                        ucrt.getTag().getTagType(),
                        ucrt.getTag().getTagValue(),
                        ucrt.getTag().getLabel()
                )).toList();

        return new SplitViewDTO(
                pdf.getResourceId(), pdf.getResourceName(), pdf.getResourcePath(),
                courseId, chapterId, notes, tags
        );
    }

    @Override
    @Transactional
    public TagDTO saveTag(SaveTagRequest request, String userEmail) {
        User     user     = getUser(userEmail);
        Resource resource = getResource(request.pdfResourceId());
        Course   course   = getCourse(request.courseId());

        Tag tag = new Tag();
        tag.setTagType(request.tagType());
        tag.setTagValue(request.tagValue());
        tag.setLabel(request.label());
        tagRepository.save(tag);

        UserCourseResourceTag ucrt = new UserCourseResourceTag();
        ucrt.setTagId(tag.getTagId());
        ucrt.setResourceId(resource.getResourceId());
        ucrt.setCourseId(course.getCourseId());
        ucrt.setUserId(user.getUserId());
        ucrtRepository.save(ucrt);

        return new TagDTO(tag.getTagId(), tag.getTagType(), tag.getTagValue(), tag.getLabel());
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + tagId));
        ucrtRepository.deleteAll(ucrtRepository.findByTagId(tagId));
        tagRepository.delete(tag);
    }

    private Resource getResource(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found: " + id));
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
    }

    private Chapter getChapter(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + id));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}