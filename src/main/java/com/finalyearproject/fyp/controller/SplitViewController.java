package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.SaveTagRequest;
import com.finalyearproject.fyp.dto.SplitViewDTO;
import com.finalyearproject.fyp.dto.TagDTO;
import com.finalyearproject.fyp.service.SplitViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SplitViewController {

    private final SplitViewService splitViewService;

    @GetMapping("/split-view/{pdfResourceId}")
    public String splitView(@PathVariable Long pdfResourceId,
                            @RequestParam Long courseId,
                            @RequestParam Long chapterId,
                            Model model) {
        SplitViewDTO data = splitViewService.getSplitViewData(pdfResourceId, courseId, chapterId);
        model.addAttribute("pdf",         Map.of(
                "resourceId",   data.pdfResourceId(),
                "resourceName", data.pdfName(),
                "resourcePath", data.pdfPath()
        ));
        model.addAttribute("courseId",    courseId);
        model.addAttribute("chapterId",   chapterId);
        model.addAttribute("courseNotes", data.notes());
        model.addAttribute("tags",        data.tags());
        return "splitView/index";
    }

    @PostMapping("/save-tag")
    @ResponseBody
    public ResponseEntity<TagDTO> saveTag(@RequestBody SaveTagRequest request,
                                          Authentication authentication) {
        String email = YourCoursesController.extractEmail(authentication);
        return ResponseEntity.ok(splitViewService.saveTag(request, email));
    }

    @DeleteMapping("/delete-tag/{tagId}")
    @ResponseBody
    public ResponseEntity<?> deleteTag(@PathVariable Long tagId) {
        splitViewService.deleteTag(tagId);
        return ResponseEntity.ok(Map.of("message", "Tag deleted"));
    }

    @GetMapping("/get-tags/{pdfResourceId}")
    @ResponseBody
    public ResponseEntity<?> getTags(@PathVariable Long pdfResourceId,
                                     @RequestParam Long courseId,
                                     @RequestParam Long chapterId) {
        SplitViewDTO data = splitViewService.getSplitViewData(pdfResourceId, courseId, chapterId);
        return ResponseEntity.ok(data.tags());
    }
}