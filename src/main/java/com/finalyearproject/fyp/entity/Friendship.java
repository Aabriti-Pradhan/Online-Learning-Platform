package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "friendship")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long friendshipId;

    // The user who sent the request
    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // The user who received the request
    @ManyToOne(optional = false)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    // PENDING, ACCEPTED, DECLINED
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}