package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.service.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Serves uploaded files at GET /files/{username}/{courseName}/{filename}
 * PDFs are served inline so they render in the browser iframe.
 */
@Controller
@RequiredArgsConstructor
public class FileServeController {

    private final LocalFileStorageService storageService;

    @GetMapping("/files/{username}/{courseName}/{filename}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String username,
            @PathVariable String courseName,
            @PathVariable String filename) throws Exception {

        String relativePath = username + "/" + courseName + "/" + filename;
        Resource file = storageService.load(relativePath);

        String contentType = "application/pdf";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            contentType = "image/" + (lower.endsWith(".png") ? "png" : "jpeg");
        } else if (lower.endsWith(".json")) {
            contentType = "application/json";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(file);
    }
}