package com.finalyearproject.fyp.service.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalyearproject.fyp.controller.ByteArrayMultipartFile;
import com.finalyearproject.fyp.dto.NoteDTO;
import com.finalyearproject.fyp.entity.Resource;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.ResourceRepository;
import com.finalyearproject.fyp.repository.UserCourseResourceRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.LocalFileStorageService;
import com.finalyearproject.fyp.service.NoteService;
import com.finalyearproject.fyp.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final ResourceService         resourceService;
    private final ResourceRepository      resourceRepository;
    private final UserRepository          userRepository;
    private final LocalFileStorageService storageService;
    private final UserCourseResourceRepository ucrRepository;
    private final ObjectMapper            objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public NoteDTO saveNote(String userEmail, Long courseId, Long chapterId,
                            String title, Object content) throws Exception {
        User   user     = getUser(userEmail);
        String json     = objectMapper.writeValueAsString(content);
        String filename = (title != null && !title.isBlank() ? title : "Untitled_Note") + ".json";
        byte[] bytes    = json.getBytes(StandardCharsets.UTF_8);

        ByteArrayMultipartFile file = new ByteArrayMultipartFile(filename, bytes);
        Resource saved = resourceService.saveNote(user.getUserId(), courseId, chapterId, file);

        return new NoteDTO(saved.getResourceId(), saved.getResourceName(), saved.getResourcePath());
    }

    @Override
    @Transactional
    public void updateNote(Long resourceId, String title, Object content) throws Exception {
        Resource resource = getResource(resourceId);

        String json  = objectMapper.writeValueAsString(content);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        Files.write(Paths.get("uploads").resolve(resource.getResourcePath()).normalize(), bytes);

        String newName = (title != null && !title.isBlank() ? title : "Untitled_Note") + ".json";
        resource.setResourceName(newName);
        resourceRepository.save(resource);
    }

    @Override
    public Object loadNote(Long resourceId) throws Exception {
        Resource resource = getResource(resourceId);
        org.springframework.core.io.Resource file = storageService.load(resource.getResourcePath());
        String json = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return objectMapper.readValue(json, Object.class);
    }

    @Override
    public String getNoteTitle(Long resourceId) {
        return resourceRepository.findById(resourceId).map(r -> {
            String name = r.getResourceName();
            return (name != null && name.endsWith(".json"))
                    ? name.substring(0, name.length() - 5) : name;
        }).orElse(null);
    }

    @Override
    public boolean isNoteOwner(Long resourceId, String userEmail) {
        User user = getUser(userEmail);
        return ucrRepository.findByUser(user)
                .stream()
                .anyMatch(ucr -> ucr.getResource().getResourceId().equals(resourceId));
    }

    private Resource getResource(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found: " + id));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}