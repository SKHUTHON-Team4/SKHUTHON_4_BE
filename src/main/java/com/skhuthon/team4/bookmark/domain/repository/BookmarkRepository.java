package com.skhuthon.team4.bookmark.domain.repository;

import com.skhuthon.team4.bookmark.domain.Bookmark;
import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 북마크 여부 확인
    boolean existsByMemberAndDiary(Member member, Diary diary);

    // 북마크 조회
    Optional<Bookmark> findByMemberAndDiary(Member member, Diary diary);

    // 내 북마크 목록
    List<Bookmark> findAllByMemberOrderByCreatedAtDesc(Member member);

    // 일기 삭제 시 북마크 삭제
    void deleteByDiary(Diary diary);
}