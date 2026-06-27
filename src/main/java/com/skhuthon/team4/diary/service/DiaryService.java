package com.skhuthon.team4.diary.service;

import com.skhuthon.team4.comment.domain.repository.CommentRepository;
import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.diary.dto.DiaryRequestDto;
import com.skhuthon.team4.diary.dto.DiaryResponseDto;
import com.skhuthon.team4.diary.dto.TodayMoodResponseDto;
import com.skhuthon.team4.global.exception.BusinessException;
import com.skhuthon.team4.global.exception.ErrorCode;
import com.skhuthon.team4.global.filter.BadWordFilter;
import com.skhuthon.team4.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final CommentRepository commentRepository;
    private final BadWordFilter badWordFilter;

    // POST /api/diaries - 일기 작성 (하루 1개 제한)
    @Transactional
    public DiaryResponseDto createDiary(Member member, DiaryRequestDto request) {
        LocalDate today = LocalDate.now();

        if (diaryRepository.existsByMemberAndDiaryDate(member, today)) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_EXISTS);
        }

        if (badWordFilter.containsBadWord(request.content())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
        }

        Diary diary = Diary.builder()
                .member(member)
                .title(request.title())
                .content(request.content())
                .diaryDate(today)
                .isPublic(request.isPublic() != null ? request.isPublic() : true)
                .emotion(request.emotion())
                .build();

        Diary saved = diaryRepository.save(diary);
        return DiaryResponseDto.from(saved, 0);
    }

    // PATCH /api/diaries/{diaryId}/emotion - 감정 업데이트
    @Transactional
    public DiaryResponseDto updateEmotion(Member member, Long diaryId, Integer emotion) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.getMember().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.DIARY_ACCESS_DENIED);
        }

        diary.updateEmotion(emotion);
        return DiaryResponseDto.from(diary, commentRepository.countByDiary(diary));
    }

    // PATCH /api/diaries/{diaryId}/ai-comment - AI 멘트 저장 (AI팀 호출용)
    @Transactional
    public DiaryResponseDto updateAiComment(Long diaryId, String aiComment) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        diary.updateAiComment(aiComment);
        return DiaryResponseDto.from(diary, commentRepository.countByDiary(diary));
    }

    // GET /api/diaries/today-mood - 홈 화면 감정 통계
    public TodayMoodResponseDto getTodayMood() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.toLocalDate().atTime(21, 0, 0);

        if (now.isAfter(end)) {
            end = end.plusDays(1);
        }
        LocalDateTime start = end.minusDays(1);

        List<Diary> diaries = diaryRepository.findByCreatedAtBetweenAndEmotionIsNotNull(start, end);

        if (diaries.isEmpty()) {
            return TodayMoodResponseDto.empty();
        }

        Map<Integer, Long> emotionCounts = diaries.stream()
                .collect(Collectors.groupingBy(Diary::getEmotion, Collectors.counting()));

        long total = diaries.size();
        int count100 = emotionCounts.getOrDefault(100, 0L).intValue();
        int count75  = emotionCounts.getOrDefault(75, 0L).intValue();
        int count50  = emotionCounts.getOrDefault(50, 0L).intValue();
        int count25  = emotionCounts.getOrDefault(25, 0L).intValue();
        int count0   = emotionCounts.getOrDefault(0, 0L).intValue();

        double avg = diaries.stream()
                .mapToInt(Diary::getEmotion)
                .average()
                .orElse(50);

        int representativeEmotion;
        if (avg >= 87.5) representativeEmotion = 100;
        else if (avg >= 62.5) representativeEmotion = 75;
        else if (avg >= 37.5) representativeEmotion = 50;
        else if (avg >= 12.5) representativeEmotion = 25;
        else representativeEmotion = 0;

        return new TodayMoodResponseDto(
                total,
                count100, count75, count50, count25, count0,
                Math.round(count100 * 100.0 / total),
                Math.round(count75  * 100.0 / total),
                Math.round(count50  * 100.0 / total),
                Math.round(count25  * 100.0 / total),
                Math.round(count0   * 100.0 / total),
                representativeEmotion,
                start,
                end
        );
    }

    // 최신순/랜덤 피드
    public List<DiaryResponseDto> getFeed(String sort) {
        List<Diary> diaries = "random".equalsIgnoreCase(sort)
                ? diaryRepository.findAllRandom()
                : diaryRepository.findAllByIsPublicTrueOrderByCreatedAtDesc();

        return diaries.stream()
                .map(d -> DiaryResponseDto.from(d, commentRepository.countByDiary(d)))
                .toList();
    }

    // 핫 피드
    public List<DiaryResponseDto> getHotFeed() {
        return diaryRepository.findTop10ByIsPublicTrueOrderByEmpathyCountDescCreatedAtDesc()
                .stream()
                .map(d -> DiaryResponseDto.from(d, commentRepository.countByDiary(d)))
                .toList();
    }

    // 내 일기 (년/월 필터 + 나만보기)
    public List<DiaryResponseDto> getMyDiaries(Member member, int year, int month, String visibility) {
        List<Diary> diaries;

        if ("private".equals(visibility)) {
            diaries = diaryRepository.findByMemberAndIsPublicFalseOrderByCreatedAtDesc(member);
        } else {
            diaries = diaryRepository.findByMemberAndYearAndMonth(member, year, month);
        }

        return diaries.stream()
                .map(d -> DiaryResponseDto.from(d, commentRepository.countByDiary(d)))
                .toList();
    }

    // DELETE /api/diaries/{id}
    @Transactional
    public void deleteDiary(Member member, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.getMember().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.DIARY_ACCESS_DENIED);
        }

        diaryRepository.delete(diary);
    }

    // GET /api/diaries/{id} - 일기 단건 조회
    public DiaryResponseDto getDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        return DiaryResponseDto.from(diary, commentRepository.countByDiary(diary));
    }

    // GET /api/diaries/search?keyword= - 제목 + 본문 검색
    public List<DiaryResponseDto> searchDiaries(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return diaryRepository.searchByKeyword(keyword.trim())
                .stream()
                .map(d -> DiaryResponseDto.from(d, commentRepository.countByDiary(d)))
                .toList();
    }

    // PATCH /api/diaries/{id} - 일기 수정
    @Transactional
    public DiaryResponseDto updateDiary(Member member, Long diaryId, DiaryRequestDto request) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.getMember().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.DIARY_ACCESS_DENIED);
        }

        if (badWordFilter.containsBadWord(request.content())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
        }

        diary.update(
                request.title() != null ? request.title() : diary.getTitle(),
                request.content() != null ? request.content() : diary.getContent(),
                request.isPublic() != null ? request.isPublic() : diary.isPublic()
        );

        return DiaryResponseDto.from(diary, commentRepository.countByDiary(diary));
    }
}