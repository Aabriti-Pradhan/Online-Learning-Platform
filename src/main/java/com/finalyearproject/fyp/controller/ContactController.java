package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.ContactMessage;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.ContactMessageRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository           userRepository;
    private final NotificationService      notificationService;

    @PostMapping("/contact/send")
    public ResponseEntity<?> send(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam String message) {

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name is required"));
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Message is required"));
        }

        ContactMessage msg = new ContactMessage();
        msg.setName(name.trim());
        msg.setEmail(email.trim());
        msg.setPhone(phone != null ? phone.trim() : null);
        msg.setMessage(message.trim());
        msg.setSentAt(LocalDateTime.now());
        msg.setRead(false);
        ContactMessage saved = contactMessageRepository.save(msg);

        // Notify all admins
        List<User> admins = userRepository.findByRole("ADMIN");
        List<Long> adminIds = admins.stream().map(User::getUserId).toList();
        if (!adminIds.isEmpty()) {
            notificationService.sendToUsers(
                    adminIds,
                    "New contact message",
                    name.trim() + " sent a message: " + message.trim(),
                    "CONTACT_MESSAGE",
                    saved.getId()
            );
        }

        return ResponseEntity.ok(Map.of("message", "Your message has been sent successfully!"));
    }
}