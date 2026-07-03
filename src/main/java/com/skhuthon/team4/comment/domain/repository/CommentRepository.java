package com.skhuthon.team4.comment.domain.repository;

import com.skhuthon.team4.comment.domain.Comment;
import com.skhuthon.team4.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 일기의 댓글 목록 (오래된순)
    List<Comment> findByDiaryOrderByCreatedAtAsc(Diary diary);

    // 특정 일기의 댓글 수
    int countByDiary(Diary diary);

    // 일기 삭제 시 댓글 일괄 삭제
    void deleteByDiary(Diary diary);
}