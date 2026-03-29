package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "user_predict")
public class UserPredict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userPredictId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;
}