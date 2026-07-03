package com.skhuthon.team4.member.dto;

public record MemberProfileDto(
        Long id,
        String nickname,
        String profileImage,
        String email,
        boolean isNotification,
        boolean notificationNight,
        boolean notificationMorning,
        boolean notificationNightEmail,
        boolean notificationNightPush,
        boolean notificationMorningEmail,
        boolean notificationMorningPush,
        int diaryCount,
        int receivedEmpathy,
        int givenEmpathy
) {}