package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.SharedTestDTO;
import com.finalyearproject.fyp.dto.ShareTestRequest;
import com.finalyearproject.fyp.service.SharedTestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SharedTestController {

    private final SharedTestService sharedTestService;

    //Share a test

    @PostMapping("/shared-tests/share")
    @ResponseBody
    public ResponseEntity<?> shareTest(@RequestBody ShareTestRequest request,
                                       Authentication auth) {
        try {
            String email = YourCoursesController.extractEmail(auth);
            sharedTestService.shareTest(email, request);
            return ResponseEntity.ok(Map.of("message", "Test shared successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    //Shared tests page

    @GetMapping("/shared-tests")
    public String sharedTestsPage(Model model,
                                  Authentication auth,
                                  HttpServletRequest request) {
        String email = YourCoursesController.extractEmail(auth);

        List<SharedTestDTO> sharedWithMe = sharedTestService.getTestsSharedWithMe(email);
        List<SharedTestDTO> sharedByMe   = sharedTestService.getTestsSharedByMe(email);

        model.addAttribute("sharedWithMe", sharedWithMe);
        model.addAttribute("sharedByMe",   sharedByMe);
        model.addAttribute("currentPath",  request.getRequestURI());
        return "sharedTests/index";
    }

    //Already shared friend IDs (for share modal)

    @GetMapping("/shared-tests/already-shared/{testId}")
    @ResponseBody
    public ResponseEntity<List<Long>> alreadyShared(@PathVariable Long testId,
                                                    Authentication auth) {
        String email = YourCoursesController.extractEmail(auth);
        return ResponseEntity.ok(sharedTestService.getAlreadySharedFriendIds(testId, email));
    }
}