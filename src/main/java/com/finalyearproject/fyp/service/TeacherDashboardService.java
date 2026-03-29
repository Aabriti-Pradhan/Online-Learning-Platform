package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.TeacherDashboardDTO;

public interface TeacherDashboardService {
    TeacherDashboardDTO getDashboard(String teacherEmail);
}