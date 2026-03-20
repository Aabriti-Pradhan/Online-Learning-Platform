package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.NotificationSummaryDTO;
import com.finalyearproject.fyp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Poll endpoint — called every 30s by the topbar JS */
    @GetMapping("/notifications/summary")
    @ResponseBody
    public ResponseEntity<NotificationSummaryDTO> getSummary(Authentication auth) {
        String email = YourCoursesController.extractEmail(auth);
        return ResponseEntity.ok(notificationService.getSummary(email));
    }

    /** Mark a single notification as read */
    @PostMapping("/notifications/{notificationId}/read")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId, Authentication auth) {
        notificationService.markAsRead(notificationId, YourCoursesController.extractEmail(auth));
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    /** Mark all as read */
    @PostMapping("/notifications/read-all")
    @ResponseBody
    public ResponseEntity<?> markAllAsRead(Authentication auth) {
        notificationService.markAllAsRead(YourCoursesController.extractEmail(auth));
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }
}