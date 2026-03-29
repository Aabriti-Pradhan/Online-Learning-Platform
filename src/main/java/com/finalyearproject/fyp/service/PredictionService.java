package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.PredictionDashboardDTO;
import com.finalyearproject.fyp.dto.MotivationDTO;

public interface PredictionService {

    /** Generate all predictions for a user (called on demand or scheduled). */
    void generatePredictions(String userEmail) throws Exception;

    /** Get the full analytics dashboard data. */
    PredictionDashboardDTO getDashboard(String userEmail);

    /** Get the latest motivational summary for the topbar. */
    MotivationDTO getMotivation(String userEmail);
}