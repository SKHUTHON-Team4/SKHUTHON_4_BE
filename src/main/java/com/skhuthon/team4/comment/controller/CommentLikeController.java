package com.skhuthon.team4.comment.controller;

import com.skhuthon.team4.comment.dto.CommentLikeResponseDto;
import com.skhuthon.team4.comment.service.CommentLikeService;
import com.skhuthon.team4.global.common.ApiResponseTemplate;
import com.skhuthon.team4.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    // 댓글 좋아요 토글 (추가/취소)
    @PostMapping("/{commentId}/like")
    public ApiResponseTemplate<CommentLikeResponseDto> toggleCommentLike(
            @AuthenticationPrincipal Member member,
            @PathVariable Long commentId
    ) {
        return ApiResponseTemplate.success(commentLikeService.toggleCommentLike(member, commentId));
    }

    // 댓글 좋아요 여부 확인
    @GetMapping("/{commentId}/like")
    public ApiResponseTemplate<CommentLikeResponseDto> getCommentLikeStatus(
            @AuthenticationPrincipal Member member,
            @PathVariable Long commentId
    ) {
        return ApiResponseTemplate.success(commentLikeService.getCommentLikeStatus(member, commentId));
    }
}