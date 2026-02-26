package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "attempt_ans")
@IdClass(AttemptAnsId.class)
public class AttemptAns {

    @Id
    private Long attemptId;

    @Id
    private Long questionId;

    private String selectedAns;

}