package com.finalyearproject.fyp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private String        title;
    private String        message;
    private String        type;         // ENROLLMENT, NEW_CHAPTER, NEW_RESOURCE, NEW_TEST, FRIEND_REQUEST, FRIEND_ACCEPTED, TEST_SUBMITTED
    private Long          referenceId;  // courseId, chapterId, testId etc. for deep linking
    private LocalDateTime createdAt;
}