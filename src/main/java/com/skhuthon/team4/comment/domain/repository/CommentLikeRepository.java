package com.skhuthon.team4.comment.domain.repository;

import com.skhuthon.team4.comment.domain.Comment;
import com.skhuthon.team4.comment.domain.CommentLike;
import com.skhuthon.team4.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 좋아요 여부 확인
    boolean existsByMemberAndComment(Member member, Comment comment);

    // 좋아요 조회
    Optional<CommentLike> findByMemberAndComment(Member member, Comment comment);

    // 댓글 삭제 시 좋아요 삭제
    void deleteByComment(Comment comment);

    // 좋아요 수 조회
    int countByComment(Comment comment);
}