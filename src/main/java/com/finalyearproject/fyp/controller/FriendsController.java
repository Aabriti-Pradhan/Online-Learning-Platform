package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.FriendsPageDTO;
import com.finalyearproject.fyp.service.FriendshipService;
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
public class FriendsController {

    private final FriendshipService friendshipService;

    // ── Friends page ──────────────────────────────────────────────────────────

    @GetMapping("/friends")
    public String friendsPage(Model model, Authentication auth, HttpServletRequest request) {
        String email = YourCoursesController.extractEmail(auth);
        FriendsPageDTO data = friendshipService.getFriendsPageData(email);

        model.addAttribute("friends",         data.friends());
        model.addAttribute("incoming",        data.incoming());
        model.addAttribute("pendingOutgoing", data.pendingOutgoing());
        model.addAttribute("friendIds",       data.friendIds());
        model.addAttribute("allUsers",        data.allUsers());
        model.addAttribute("currentPath",     request.getRequestURI());
        return "friends/index";
    }

    // ── Send friend request ───────────────────────────────────────────────────

    @PostMapping("/friends/request/{targetUserId}")
    @ResponseBody
    public ResponseEntity<?> sendRequest(@PathVariable Long targetUserId, Authentication auth) {
        try {
            friendshipService.sendRequest(YourCoursesController.extractEmail(auth), targetUserId);
            return ResponseEntity.ok(Map.of("message", "Request sent"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Accept friend request ─────────────────────────────────────────────────

    @PostMapping("/friends/accept/{friendshipId}")
    @ResponseBody
    public ResponseEntity<?> acceptRequest(@PathVariable Long friendshipId, Authentication auth) {
        try {
            friendshipService.acceptRequest(friendshipId, YourCoursesController.extractEmail(auth));
            return ResponseEntity.ok(Map.of("message", "Friend request accepted"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Decline friend request ────────────────────────────────────────────────

    @PostMapping("/friends/decline/{friendshipId}")
    @ResponseBody
    public ResponseEntity<?> declineRequest(@PathVariable Long friendshipId, Authentication auth) {
        try {
            friendshipService.declineRequest(friendshipId, YourCoursesController.extractEmail(auth));
            return ResponseEntity.ok(Map.of("message", "Request declined"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Remove friend ─────────────────────────────────────────────────────────

    @DeleteMapping("/friends/{friendshipId}")
    @ResponseBody
    public ResponseEntity<?> removeFriend(@PathVariable Long friendshipId, Authentication auth) {
        try {
            friendshipService.removeFriend(friendshipId, YourCoursesController.extractEmail(auth));
            return ResponseEntity.ok(Map.of("message", "Friend removed"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Cancel outgoing request ───────────────────────────────────────────────

    @DeleteMapping("/friends/cancel/{targetUserId}")
    @ResponseBody
    public ResponseEntity<?> cancelRequest(@PathVariable Long targetUserId, Authentication auth) {
        friendshipService.cancelRequest(YourCoursesController.extractEmail(auth), targetUserId);
        return ResponseEntity.ok(Map.of("message", "Request cancelled"));
    }
}