package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.AdminCourseDTO;
import com.finalyearproject.fyp.dto.AdminStatsDTO;
import com.finalyearproject.fyp.entity.User;

import java.util.List;

public interface AdminService {

    AdminStatsDTO getStats();
    List<User> getAllUsers(String roleFilter, String search);
    User setUserActive(Long userId, boolean active);
    User setUserRole(Long userId, String role);
    List<AdminCourseDTO> getAllCoursesForAdmin();
    void deleteCourseAsAdmin(Long courseId);
}