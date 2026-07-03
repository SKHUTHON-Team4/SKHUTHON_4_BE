package com.skhuthon.team4.bookmark.controller;

import com.skhuthon.team4.bookmark.dto.BookmarkResponseDto;
import com.skhuthon.team4.bookmark.service.BookmarkService;
import com.skhuthon.team4.global.common.ApiResponseTemplate;
import com.skhuthon.team4.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 북마크 토글 (추가/취소)
    @PostMapping("/{diaryId}")
    public ApiResponseTemplate<Map<String, Boolean>> toggleBookmark(
            @AuthenticationPrincipal Member member,
            @PathVariable Long diaryId
    ) {
        boolean isBookmarked = bookmarkService.toggleBookmark(member, diaryId);
        return ApiResponseTemplate.success(Map.of("isBookmarked", isBookmarked));
    }

    // 내 북마크 목록
    @GetMapping
    public ApiResponseTemplate<List<BookmarkResponseDto>> getMyBookmarks(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(bookmarkService.getMyBookmarks(member));
    }

    // 북마크 여부 확인
    @GetMapping("/{diaryId}")
    public ApiResponseTemplate<Map<String, Boolean>> isBookmarked(
            @AuthenticationPrincipal Member member,
            @PathVariable Long diaryId
    ) {
        boolean isBookmarked = bookmarkService.isBookmarked(member, diaryId);
        return ApiResponseTemplate.success(Map.of("isBookmarked", isBookmarked));
    }
}