package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.NoteDTO;
import com.finalyearproject.fyp.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NotesEditorController {

    private final NoteService noteService;

    @GetMapping("/notes-editor")
    public String openNotesEditor(@RequestParam(required = false) Long courseId,
                                  @RequestParam(required = false) Long chapterId,
                                  @RequestParam(required = false) Long resourceId,
                                  Model model,
                                  HttpServletRequest request,
                                  Authentication authentication) {

        model.addAttribute("courseId",    courseId);
        model.addAttribute("chapterId",   chapterId);
        model.addAttribute("resourceId",  resourceId);
        model.addAttribute("currentPath", request.getRequestURI());

        boolean readOnly = false;
        if (resourceId != null) {
            model.addAttribute("noteTitle", noteService.getNoteTitle(resourceId));
            boolean isStudent = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
            // Read-only only if student is viewing a note they do NOT own
            if (isStudent) {
                String email = YourCoursesController.extractEmail(authentication);
                readOnly = !noteService.isNoteOwner(resourceId, email);
            }
        }

        model.addAttribute("readOnly", readOnly);
        return "notesEditor/index";
    }

    @PostMapping("/save-note")
    @ResponseBody
    public ResponseEntity<?> saveNote(@RequestBody Map<String, Object> payload,
                                      Authentication authentication) throws Exception {
        String email     = YourCoursesController.extractEmail(authentication);
        String title     = (String) payload.get("title");
        Object content   = payload.get("content");
        Long   courseId  = Long.valueOf(payload.get("courseId").toString());
        Long   chapterId = Long.valueOf(payload.get("chapterId").toString());

        NoteDTO saved = noteService.saveNote(email, courseId, chapterId, title, content);
        return ResponseEntity.ok(Map.of(
                "resourceId",   saved.resourceId(),
                "resourceName", saved.resourceName(),
                "resourcePath", saved.resourcePath()
        ));
    }

    @PutMapping("/update-note/{resourceId}")
    @ResponseBody
    public ResponseEntity<?> updateNote(@PathVariable Long resourceId,
                                        @RequestBody Map<String, Object> payload) throws Exception {
        noteService.updateNote(resourceId, (String) payload.get("title"), payload.get("content"));
        return ResponseEntity.ok(Map.of("message", "Note updated"));
    }

    @GetMapping("/load-note/{resourceId}")
    @ResponseBody
    public ResponseEntity<Object> loadNote(@PathVariable Long resourceId) throws Exception {
        return ResponseEntity.ok(noteService.loadNote(resourceId));
    }
}