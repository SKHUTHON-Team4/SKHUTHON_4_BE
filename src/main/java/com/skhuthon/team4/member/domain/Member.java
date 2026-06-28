package com.skhuthon.team4.member.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Member {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "nickname", nullable = false, length = 10)
    private String nickname;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "is_notification", nullable = false)
    @Builder.Default
    private boolean isNotification = true;

    @Column(name = "is_notification_night", nullable = false)
    @Builder.Default
    private boolean isNotificationNight = true;

    @Column(name = "is_notification_morning", nullable = false)
    @Builder.Default
    private boolean isNotificationMorning = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private Integer age;

    public void updateAge(Integer age) {
        this.age = age;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateNotification(boolean isNotification) {
        this.isNotification = isNotification;
    }

    public void updateNotificationNight(boolean isNotificationNight) {
        this.isNotificationNight = isNotificationNight;
    }

    public void updateNotificationMorning(boolean isNotificationMorning) {
        this.isNotificationMorning = isNotificationMorning;
    }
}