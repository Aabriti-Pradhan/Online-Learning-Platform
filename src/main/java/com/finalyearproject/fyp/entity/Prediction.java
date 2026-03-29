package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "prediction")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long predictionId;

    private String        predictionType; // MOTIVATION, STUDY_PLAN, WEAK_AREAS
    @Column(columnDefinition = "TEXT")
    private String        value;
    private LocalDateTime generatedAt;

}