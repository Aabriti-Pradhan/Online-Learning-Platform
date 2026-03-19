package com.finalyearproject.fyp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalyearproject.fyp.entity.Resource;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.ResourceRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.LocalFileStorageService;
import com.finalyearproject.fyp.service.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NotesEditorController {

    private final UserRepository          userRepository;
    private final ResourceService         resourceService;
    private final ResourceRepository      resourceRepository;
    private final LocalFileStorageService storageService;

    /**
     * GET /notes-editor
     * - ?courseId=X&chapterId=Y   → new note for that chapter
     * - ?resourceId=X             → open existing note for editing
     */
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
            resourceRepository.findById(resourceId).ifPresent(r -> {
                String name = r.getResourceName();
                if (name != null && name.endsWith(".json")) {
                    name = name.substring(0, name.length() - 5);
                }
                model.addAttribute("noteTitle", name);
            });

            // If the logged-in user is a student, mark as read-only
            boolean isStudent = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
            if (isStudent) readOnly = true;
        }

        model.addAttribute("readOnly", readOnly);
        return "notesEditor/index";
    }

    // ── Create new note ───────────────────────────────────────────────────────

    @PostMapping("/save-note")
    @ResponseBody
    public ResponseEntity<?> saveNote(@RequestBody Map<String, Object> payload,
                                      Authentication authentication) throws Exception {

        String title     = (String) payload.get("title");
        Object content   = payload.get("content");
        Long   courseId  = Long.valueOf(payload.get("courseId").toString());
        Long   chapterId = Long.valueOf(payload.get("chapterId").toString());

        String json     = new ObjectMapper().writeValueAsString(content);
        String filename = (title != null && !title.isBlank() ? title : "Untitled_Note") + ".json";
        byte[] bytes    = json.getBytes(StandardCharsets.UTF_8);

        User user = resolveUser(authentication);
        ByteArrayMultipartFile file = new ByteArrayMultipartFile(filename, bytes);
        Resource saved = resourceService.saveNote(user.getUserId(), courseId, chapterId, file);

        return ResponseEntity.ok(Map.of(
                "resourceId",   saved.getResourceId(),
                "resourceName", saved.getResourceName(),
                "resourcePath", saved.getResourcePath()
        ));
    }

    // ── Update existing note ──────────────────────────────────────────────────

    @PutMapping("/update-note/{resourceId}")
    @ResponseBody
    public ResponseEntity<?> updateNote(@PathVariable Long resourceId,
                                        @RequestBody Map<String, Object> payload) throws Exception {

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Note not found: " + resourceId));

        Object content = payload.get("content");
        String title   = (String) payload.get("title");

        String json  = new ObjectMapper().writeValueAsString(content);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        Path filePath = Paths.get("uploads").resolve(resource.getResourcePath()).normalize();
        Files.write(filePath, bytes);

        String newName = (title != null && !title.isBlank() ? title : "Untitled_Note") + ".json";
        resource.setResourceName(newName);
        resourceRepository.save(resource);

        return ResponseEntity.ok(Map.of("message", "Note updated"));
    }

    // ── Load note content ─────────────────────────────────────────────────────

    @GetMapping("/load-note/{resourceId}")
    @ResponseBody
    public ResponseEntity<Object> loadNote(@PathVariable Long resourceId) throws Exception {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Note not found: " + resourceId));

        org.springframework.core.io.Resource file = storageService.load(resource.getResourcePath());
        String json = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Object parsed = new ObjectMapper().readValue(json, Object.class);
        return ResponseEntity.ok(parsed);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User resolveUser(Authentication auth) {
        String email = YourCoursesController.extractEmail(auth);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
