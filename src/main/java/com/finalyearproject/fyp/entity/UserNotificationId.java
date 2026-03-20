package com.finalyearproject.fyp.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class UserNotificationId implements Serializable {
    private Long notificationId;
    private Long userId;
}