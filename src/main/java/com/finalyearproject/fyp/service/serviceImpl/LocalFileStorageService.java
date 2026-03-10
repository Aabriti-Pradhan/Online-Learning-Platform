package com.finalyearproject.fyp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Phase 1 — Local disk storage replacing Google Drive.
 *
 * Files are stored at:  uploads/{username}/{courseName}/{uuid}_{originalName}
 * Files are served via: GET /files/{username}/{courseName}/{uuid}_{filename}
 *
 * Set  file.upload-dir=uploads  in application.properties.
 */
@Service
public class LocalFileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
    }

    /**
     * Store a file under uploads/{username}/{courseName}/ and return the relative path.
     * Returned path format:  {username}/{courseName}/{uuid}_{sanitizedName}
     */
    public String store(MultipartFile file, String username, String courseName) throws IOException {
        String sanitizedUser   = sanitize(username);
        String sanitizedCourse = sanitize(courseName);
        String sanitizedFile   = sanitize(file.getOriginalFilename());
        String unique          = UUID.randomUUID().toString().substring(0, 8) + "_" + sanitizedFile;

        Path dir = Paths.get(uploadDir, sanitizedUser, sanitizedCourse);
        Files.createDirectories(dir);

        Path dest = dir.resolve(unique);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        // relative path stored in DB — mirrors the URL pattern
        return sanitizedUser + "/" + sanitizedCourse + "/" + unique;
    }

    /**
     * Load a file as a Spring Resource for serving via HTTP.
     * relativePath is exactly what was returned from store().
     */
    public Resource load(String relativePath) throws MalformedURLException {
        Path file = Paths.get(uploadDir).resolve(relativePath).normalize();
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new RuntimeException("File not found: " + relativePath);
    }

    /**
     * Delete a file from disk.
     */
    public void delete(String relativePath) throws IOException {
        Path file = Paths.get(uploadDir).resolve(relativePath).normalize();
        Files.deleteIfExists(file);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String sanitize(String name) {
        if (name == null) return "unknown";
        return name.trim()
                .replaceAll("\\s+", "_")          // spaces → underscores
                .replaceAll("[^a-zA-Z0-9._-]", ""); // remove unsafe chars
    }
}