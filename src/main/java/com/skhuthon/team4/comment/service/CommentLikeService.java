package com.skhuthon.team4.comment.service;

import com.skhuthon.team4.comment.domain.Comment;
import com.skhuthon.team4.comment.domain.CommentLike;
import com.skhuthon.team4.comment.domain.repository.CommentLikeRepository;
import com.skhuthon.team4.comment.domain.repository.CommentRepository;
import com.skhuthon.team4.comment.dto.CommentLikeResponseDto;
import com.skhuthon.team4.global.exception.BusinessException;
import com.skhuthon.team4.global.exception.ErrorCode;
import com.skhuthon.team4.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    // 댓글 좋아요 토글 (추가/취소)
    @Transactional
    public CommentLikeResponseDto toggleCommentLike(Member member, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        Optional<CommentLike> existing = commentLikeRepository.findByMemberAndComment(member, comment);

        if (existing.isPresent()) {
            // 이미 좋아요 → 취소
            commentLikeRepository.delete(existing.get());
        } else {
            // 좋아요 추가
            CommentLike commentLike = CommentLike.builder()
                    .member(member)
                    .comment(comment)
                    .build();
            commentLikeRepository.save(commentLike);
        }

        int likeCount = commentLikeRepository.countByComment(comment);
        boolean isLiked = commentLikeRepository.existsByMemberAndComment(member, comment);

        return new CommentLikeResponseDto(commentId, isLiked, likeCount);
    }

    // 좋아요 여부 확인
    public CommentLikeResponseDto getCommentLikeStatus(Member member, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        boolean isLiked = commentLikeRepository.existsByMemberAndComment(member, comment);
        int likeCount = commentLikeRepository.countByComment(comment);

        return new CommentLikeResponseDto(commentId, isLiked, likeCount);
    }
}