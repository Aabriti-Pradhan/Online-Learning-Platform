package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.MotivationDTO;
import com.finalyearproject.fyp.dto.PredictionDashboardDTO;
import com.finalyearproject.fyp.service.PredictionService;
import com.finalyearproject.fyp.service.TeacherDashboardService;
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
public class PredictionController {

    private final PredictionService        predictionService;
    private final TeacherDashboardService  teacherDashboardService;

    @GetMapping("/progress-tracker")
    public String progressTracker(Model model,
                                  Authentication auth,
                                  HttpServletRequest request) {
        String email     = YourCoursesController.extractEmail(auth);
        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        model.addAttribute("currentPath", request.getRequestURI());

        if (isTeacher) {
            var teacherDash = teacherDashboardService.getDashboard(email);
            long coursesAbove70 = teacherDash.courses().stream()
                    .filter(c -> c.classAvgPct() >= 70).count();
            model.addAttribute("dashboard", teacherDash);
            model.addAttribute("coursesAbove70", coursesAbove70);
            return "progressTracker/teacher";
        } else {
            model.addAttribute("dashboard", predictionService.getDashboard(email));
            return "progressTracker/index";
        }
    }

    @GetMapping("/prediction/motivation")
    @ResponseBody
    public ResponseEntity<MotivationDTO> getMotivation(Authentication auth) {
        String email = YourCoursesController.extractEmail(auth);
        return ResponseEntity.ok(predictionService.getMotivation(email));
    }

    @PostMapping("/prediction/generate")
    @ResponseBody
    public ResponseEntity<?> generate(Authentication auth) {
        try {
            String email = YourCoursesController.extractEmail(auth);
            predictionService.generatePredictions(email);
            return ResponseEntity.ok(Map.of("message", "Predictions generated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}