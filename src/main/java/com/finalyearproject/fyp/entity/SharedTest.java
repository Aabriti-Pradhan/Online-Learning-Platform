package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "shared_test",
        uniqueConstraints = @UniqueConstraint(columnNames = {"test_id", "shared_by_user_id", "shared_to_user_id"}))
public class SharedTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedTestId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shared_by_user_id", nullable = false)
    private User sharedBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shared_to_user_id", nullable = false)
    private User sharedTo;

    private LocalDateTime sharedAt;
}