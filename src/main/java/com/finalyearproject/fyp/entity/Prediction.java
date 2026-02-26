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
    private Long predictionId;

    private String predictionType;
    private String value;
    private LocalDateTime generatedAt;

    @OneToOne
    @MapsId
    @JoinColumn(name = "predictionId")
    private UserPredict userPredict;

}