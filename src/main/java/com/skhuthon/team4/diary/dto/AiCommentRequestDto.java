package com.skhuthon.team4.diary.dto;

import java.util.List;

public record AiCommentRequestDto(
        List<Recommendation> recommendations
) {
    public record Recommendation(
            Long id,
            String aiComment
    ) {}
}