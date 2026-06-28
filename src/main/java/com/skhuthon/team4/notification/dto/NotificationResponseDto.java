package com.skhuthon.team4.notification.dto;

import com.skhuthon.team4.notification.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        String type,
        String message,
        Long diaryId,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getDiaryId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}