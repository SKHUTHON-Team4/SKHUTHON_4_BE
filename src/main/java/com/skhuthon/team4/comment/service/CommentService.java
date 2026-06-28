package com.skhuthon.team4.comment.service;

import com.skhuthon.team4.comment.domain.Comment;
import com.skhuthon.team4.comment.domain.repository.CommentRepository;
import com.skhuthon.team4.comment.dto.CommentRequestDto;
import com.skhuthon.team4.comment.dto.CommentResponseDto;
import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.global.exception.BusinessException;
import com.skhuthon.team4.global.exception.ErrorCode;
import com.skhuthon.team4.global.filter.BadWordFilter;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final DiaryRepository diaryRepository;
    private final BadWordFilter badWordFilter;
    private final NotificationService notificationService;

    // POST /api/diaries/{diaryId}/comments - 댓글 작성
    @Transactional
    public CommentResponseDto createComment(Member member, Long diaryId, CommentRequestDto request) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        if (badWordFilter.containsBadWord(request.content())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
        }

        Comment comment = Comment.builder()
                .member(member)
                .diary(diary)
                .content(request.content())
                .build();

        CommentResponseDto response = CommentResponseDto.from(commentRepository.save(comment));

        // 본인 일기가 아닐 때만 알림 저장
        if (!diary.getMember().getId().equals(member.getId())) {
            notificationService.saveNotification(
                    diary.getMember(),
                    "COMMENT",
                    member.getNickname() + "님이 회원님의 일기에 댓글을 남겼어요 💬",
                    diaryId
            );
        }

        return response;
    }

    // GET /api/diaries/{diaryId}/comments - 댓글 목록 조회
    public List<CommentResponseDto> getComments(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        return commentRepository.findByDiaryOrderByCreatedAtDesc(diary)
                .stream()
                .map(CommentResponseDto::from)
                .toList();
    }

    // DELETE /api/comments/{commentId} - 댓글 삭제
    @Transactional
    public void deleteComment(Member member, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        commentRepository.delete(comment);
    }
}