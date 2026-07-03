package com.skhuthon.team4.bookmark.dto;

import com.skhuthon.team4.bookmark.domain.Bookmark;

public record BookmarkResponseDto(
        Long id,
        Long diaryId,
        String title,
        String content,
        String nickname,
        String diaryDate,
        boolean isPublic,
        int empathyCount,
        int commentCount
) {
    public static BookmarkResponseDto from(Bookmark bookmark, int commentCount) {
        return new BookmarkResponseDto(
                bookmark.getId(),
                bookmark.getDiary().getId(),
                bookmark.getDiary().getTitle(),
                bookmark.getDiary().getContent(),
                bookmark.getDiary().getMember().getNickname(),
                bookmark.getDiary().getDiaryDate().toString(),
                bookmark.getDiary().isPublic(),
                bookmark.getDiary().getEmpathyCount(),
                commentCount
        );
    }
}