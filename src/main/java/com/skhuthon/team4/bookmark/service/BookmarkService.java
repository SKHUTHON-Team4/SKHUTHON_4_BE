package com.skhuthon.team4.bookmark.service;

import com.skhuthon.team4.bookmark.domain.Bookmark;
import com.skhuthon.team4.bookmark.domain.repository.BookmarkRepository;
import com.skhuthon.team4.bookmark.dto.BookmarkResponseDto;
import com.skhuthon.team4.comment.domain.repository.CommentRepository;
import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.global.exception.BusinessException;
import com.skhuthon.team4.global.exception.ErrorCode;
import com.skhuthon.team4.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final DiaryRepository diaryRepository;
    private final CommentRepository commentRepository;

    // 북마크 토글 (추가/취소)
    @Transactional
    public boolean toggleBookmark(Member member, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        Optional<Bookmark> existing = bookmarkRepository.findByMemberAndDiary(member, diary);

        if (existing.isPresent()) {
            // 이미 북마크 → 취소
            bookmarkRepository.delete(existing.get());
            return false;
        } else {
            // 북마크 추가
            Bookmark bookmark = Bookmark.builder()
                    .member(member)
                    .diary(diary)
                    .build();
            bookmarkRepository.save(bookmark);
            return true;
        }
    }

    // 내 북마크 목록 조회
    public List<BookmarkResponseDto> getMyBookmarks(Member member) {
        return bookmarkRepository.findAllByMemberOrderByCreatedAtDesc(member)
                .stream()
                .map(b -> BookmarkResponseDto.from(b, commentRepository.countByDiary(b.getDiary())))
                .toList();
    }

    // 북마크 여부 확인
    public boolean isBookmarked(Member member, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));
        return bookmarkRepository.existsByMemberAndDiary(member, diary);
    }
}