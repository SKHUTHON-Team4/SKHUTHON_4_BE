package com.skhuthon.team4.diary.dto;

import com.skhuthon.team4.diary.domain.Diary;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryResponseDto(
        Long id,
        Long memberId,
        String nickname,
        String profileImage,
        String title,
        String content,
        LocalDate diaryDate,
        int empathyCount,
        int commentCount,
        boolean isPublic,
        Integer emotion,
        String aiComment,
        LocalDateTime createdAt
) {
    public static DiaryResponseDto from(Diary diary, int commentCount) {
        return new DiaryResponseDto(
                diary.getId(),
                diary.getMember().getId(),
                diary.getMember().getNickname(),
                diary.getMember().getProfileImage(),
                diary.getTitle(),
                diary.getContent(),
                diary.getDiaryDate(),
                diary.getEmpathyCount(),
                commentCount,
                diary.isPublic(),
                diary.getEmotion(),
                diary.getAiComment(),
                diary.getCreatedAt()
        );
    }

    public static DiaryResponseDto from(Diary diary) {
        return from(diary, 0);
    }
}