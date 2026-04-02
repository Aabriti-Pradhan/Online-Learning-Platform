package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.AdminCourseDTO;
import com.finalyearproject.fyp.dto.AdminStatsDTO;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // Dashboard (stats)

    @GetMapping({"", "/"})
    public String dashboard(Model model, HttpServletRequest request) {
        AdminStatsDTO stats = adminService.getStats();
        model.addAttribute("stats",       stats);
        model.addAttribute("currentPath", "/admin");
        return "admin/index";
    }

    // User management

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String role,
                        @RequestParam(required = false) String search,
                        Model model,
                        HttpServletRequest request) {

        List<User> users = adminService.getAllUsers(role, search);
        model.addAttribute("users",       users);
        model.addAttribute("roleFilter",  role   != null ? role   : "");
        model.addAttribute("search",      search != null ? search : "");
        model.addAttribute("currentPath", "/admin/users");
        return "admin/users";
    }

    @PostMapping("/users/{userId}/toggle-active")
    @ResponseBody
    public ResponseEntity<?> toggleActive(@PathVariable Long userId,
                                          @RequestParam boolean active) {
        try {
            User updated = adminService.setUserActive(userId, active);
            return ResponseEntity.ok(Map.of(
                    "userId",   updated.getUserId(),
                    "isActive", updated.isActive(),
                    "message",  active ? "User activated" : "User deactivated"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/change-role")
    @ResponseBody
    public ResponseEntity<?> changeRole(@PathVariable Long userId,
                                        @RequestParam String role) {
        try {
            User updated = adminService.setUserRole(userId, role);
            return ResponseEntity.ok(Map.of(
                    "userId",  updated.getUserId(),
                    "role",    updated.getRole(),
                    "message", "Role updated to " + updated.getRole()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Course oversight

    @GetMapping("/courses")
    public String courses(Model model, HttpServletRequest request) {
        List<AdminCourseDTO> courses = adminService.getAllCoursesForAdmin();
        model.addAttribute("courses",     courses);
        model.addAttribute("currentPath", "/admin/courses");
        return "admin/courses";
    }

    @DeleteMapping("/courses/{courseId}")
    @ResponseBody
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            adminService.deleteCourseAsAdmin(courseId);
            return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}