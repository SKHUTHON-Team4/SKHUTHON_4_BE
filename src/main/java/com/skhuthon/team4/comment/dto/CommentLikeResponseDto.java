package com.skhuthon.team4.comment.dto;

public record CommentLikeResponseDto(
        Long commentId,
        boolean isLiked,
        int likeCount
) {}