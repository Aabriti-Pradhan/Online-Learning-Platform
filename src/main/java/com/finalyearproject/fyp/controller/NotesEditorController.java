package com.finalyearproject.fyp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalyearproject.fyp.entity.Resource;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.ResourceRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.LocalFileStorageService;
import com.finalyearproject.fyp.service.ResourceService;
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
     * - No params         → new blank note
     * - ?courseId=X       → new note for that course
     * - ?resourceId=X     → open existing note for editing
     */
    @GetMapping("/notes-editor")
    public String openNotesEditor(@RequestParam(required = false) Long courseId,
                                  @RequestParam(required = false) Long resourceId,
                                  Model model) {

        model.addAttribute("courseId",    courseId);
        model.addAttribute("resourceId",  resourceId);

        // Pre-fill title if editing an existing note
        if (resourceId != null) {
            resourceRepository.findById(resourceId).ifPresent(r -> {
                // Strip the .json extension for display
                String name = r.getResourceName();
                if (name != null && name.endsWith(".json")) {
                    name = name.substring(0, name.length() - 5);
                }
                model.addAttribute("noteTitle", name);
            });
        }

        return "notesEditor/index";
    }

    // ── Create new note ───────────────────────────────────────────────────────

    @PostMapping("/save-note")
    @ResponseBody
    public ResponseEntity<?> saveNote(@RequestBody Map<String, Object> payload,
                                      Authentication authentication) throws Exception {

        String title    = (String) payload.get("title");
        Object content  = payload.get("content");
        Long   courseId = Long.valueOf(payload.get("courseId").toString());

        String json     = new ObjectMapper().writeValueAsString(content);
        String filename = (title != null && !title.isBlank() ? title : "Untitled_Note") + ".json";
        byte[] bytes    = json.getBytes(StandardCharsets.UTF_8);

        User user = resolveUser(authentication);
        ByteArrayMultipartFile file = new ByteArrayMultipartFile(filename, bytes);
        Resource saved = resourceService.saveNote(user.getUserId(), courseId, file);

        return ResponseEntity.ok(Map.of(
                "resourceId",   saved.getResourceId(),
                "resourceName", saved.getResourceName(),
                "resourcePath", saved.getResourcePath()
        ));
    }

    // ── Update existing note (overwrite file on disk) ─────────────────────────

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

        // Overwrite the existing file on disk
        Path filePath = Paths.get("uploads").resolve(resource.getResourcePath()).normalize();
        Files.write(filePath, bytes);

        // Update resource name if title changed
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

        // Return parsed JSON so EditorJS can render it directly
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