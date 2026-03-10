package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SplitViewController {

    private final ResourceRepository             resourceRepository;
    private final UserRepository                 userRepository;
    private final CourseRepository               courseRepository;
    private final UserCourseResourceRepository   ucrRepository;
    private final TagRepository                  tagRepository;
    private final UserCourseResourceTagRepository ucrtRepository;


    @GetMapping("/split-view/{pdfResourceId}")
    public String splitView(@PathVariable Long pdfResourceId,
                            @RequestParam Long courseId,
                            Model model,
                            Authentication authentication) {

        Resource pdf = resourceRepository.findById(pdfResourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        // All notes in this course the user can pick from
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<Resource> courseNotes = ucrRepository.findByCourse(course)
                .stream()
                .map(UserCourseResource::getResource)
                .filter(r -> "Note".equalsIgnoreCase(r.getResourceType()))
                .toList();

        // Existing tags for this PDF
        List<Map<String, Object>> tags = ucrtRepository.findByResource(pdf)
                .stream()
                .map(ucrt -> {
                    Tag t = ucrt.getTag();
                    return Map.<String, Object>of(
                            "tagId",    t.getTagId(),
                            "tagType",  t.getTagType(),   // "PAGE" or "TEXT"
                            "tagValue", t.getTagValue(),  // JSON: {page, x, y, selectedText, noteBlockId}
                            "label",    t.getLabel()
                    );
                })
                .toList();

        model.addAttribute("pdf",        pdf);
        model.addAttribute("courseId",   courseId);
        model.addAttribute("courseNotes", courseNotes);
        model.addAttribute("tags",       tags);

        return "splitView/index";
    }

    @PostMapping("/save-tag")
    @ResponseBody
    public ResponseEntity<?> saveTag(@RequestBody Map<String, Object> payload,
                                     Authentication authentication) {

        Long   pdfResourceId = Long.valueOf(payload.get("pdfResourceId").toString());
        Long   courseId      = Long.valueOf(payload.get("courseId").toString());
        String tagType       = (String) payload.get("tagType");   // "PAGE" or "TEXT"
        String tagValue      = (String) payload.get("tagValue");  // JSON string
        String label         = (String) payload.get("label");

        String email = YourCoursesController.extractEmail(authentication);
        User   user  = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resource resource = resourceRepository.findById(pdfResourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Save Tag
        Tag tag = new Tag();
        tag.setTagType(tagType);
        tag.setTagValue(tagValue);
        tag.setLabel(label);
        tagRepository.save(tag);

        // Save join row
        UserCourseResourceTag ucrt = new UserCourseResourceTag();
        ucrt.setTagId(tag.getTagId());
        ucrt.setResourceId(resource.getResourceId());
        ucrt.setCourseId(course.getCourseId());
        ucrt.setUserId(user.getUserId());
        ucrtRepository.save(ucrt);

        return ResponseEntity.ok(Map.of(
                "tagId",    tag.getTagId(),
                "tagType",  tag.getTagType(),
                "tagValue", tag.getTagValue(),
                "label",    tag.getLabel()
        ));
    }

    @DeleteMapping("/delete-tag/{tagId}")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteTag(@PathVariable Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + tagId));

        // Delete all join rows referencing this tag first (FK constraint)
        List<UserCourseResourceTag> joinRows = ucrtRepository.findByTagId(tagId);
        ucrtRepository.deleteAll(joinRows);

        tagRepository.delete(tag);
        return ResponseEntity.ok(Map.of("message", "Tag deleted"));
    }

    @GetMapping("/get-tags/{pdfResourceId}")
    @ResponseBody
    public ResponseEntity<?> getTags(@PathVariable Long pdfResourceId) {
        Resource pdf = resourceRepository.findById(pdfResourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        List<Map<String, Object>> tags = ucrtRepository.findByResource(pdf)
                .stream()
                .map(ucrt -> {
                    Tag t = ucrt.getTag();
                    return Map.<String, Object>of(
                            "tagId",    t.getTagId(),
                            "tagType",  t.getTagType(),
                            "tagValue", t.getTagValue(),
                            "label",    t.getLabel()
                    );
                })
                .toList();

        return ResponseEntity.ok(tags);
    }
}