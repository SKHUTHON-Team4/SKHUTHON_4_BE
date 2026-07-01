package com.skhuthon.team4.diary.dto;

public record AiDiaryResponseDto(
        Long id,
        Integer age,
        String content,
        boolean isPublic
) {}