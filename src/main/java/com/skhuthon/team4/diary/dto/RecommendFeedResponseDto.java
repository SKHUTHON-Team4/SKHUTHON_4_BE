package com.skhuthon.team4.diary.dto;

import java.util.List;

public record RecommendFeedResponseDto(
        List<DiaryResponseDto> diaries,
        String message  // 일기 부족 시 메시지, 정상이면 null
) {}