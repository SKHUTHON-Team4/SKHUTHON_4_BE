package com.skhuthon.team4.diary.service;

import com.skhuthon.team4.comment.domain.repository.CommentRepository;
import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.diary.dto.DiaryRequestDto;
import com.skhuthon.team4.diary.dto.DiaryResponseDto;
import com.skhuthon.team4.diary.dto.TodayMoodResponseDto;
import com.skhuthon.team4.empathy.domain.repository.EmpathyRepository;
import com.skhuthon.team4.global.exception.BusinessException;
import com.skhuthon.team4.global.exception.ErrorCode;
import com.skhuthon.team4.global.filter.BadWordFilter;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.notification.domain.repository.NotificationRepository;
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
    private final EmpathyRepository empathyRepository;
    private final NotificationRepository notificationRepository;
    private final BadWordFilter badWordFilter;

    // 연령층 분류
    private String getAgeGroup(Integer age) {
        if (age == null) return "전체";
        if (age <= 19) return "고등학생";
        if (age <= 23) return "20대 초반";
        if (age <= 26) return "20대 중반";
        if (age <= 33) return "20대 후반~30대 초반";
        return "30대 중반 이상";
    }

    // 연령층 + 감정에 따른 메시지
    private String getMoodMessage(String ageGroup, int representativeEmotion) {
        return switch (ageGroup) {
            case "고등학생" -> switch (representativeEmotion) {
                case 100 -> "오늘 수업도 열심히 했군요! 노력은 배신하지 않아요 📚";
                case 75  -> "학교생활 잘 해내고 있어요! 대견해요 😊";
                case 50  -> "공부가 힘들 때도 있죠. 조금씩 나아가면 돼요 💪";
                case 25  -> "오늘 많이 지쳤나요? 잠깐 쉬어가도 괜찮아요 🌱";
                default  -> "학업 스트레스 정말 힘들죠. 오늘 하루 푹 쉬어요 🌙";
            };
            case "20대 초반" -> switch (representativeEmotion) {
                case 100 -> "대학생활 즐기고 있군요! 이 시간이 소중한 추억이 될 거예요 🎓";
                case 75  -> "캠퍼스 라이프 나쁘지 않죠? 오늘도 잘 했어요 😊";
                case 50  -> "과제와 시험 사이에서 고군분투 중이죠? 파이팅! 💪";
                case 25  -> "대학생활이 생각보다 쉽지 않죠. 그래도 잘 버티고 있어요 🌱";
                default  -> "많이 지쳤군요. 친구들이랑 잠깐 바람 쐬는 건 어때요? 🌙";
            };
            case "20대 중반" -> switch (representativeEmotion) {
                case 100 -> "취준 잘 되고 있군요! 그 에너지 계속 유지해요 ✨";
                case 75  -> "오늘 하루도 잘 준비했어요! 좋은 결과 있을 거예요 😊";
                case 50  -> "취업 준비 쉽지 않죠. 한 걸음씩 나아가면 돼요 💪";
                case 25  -> "취준이 길어지면 지치죠. 오늘만큼은 나를 위한 시간 가져요 🌱";
                default  -> "많이 힘드시죠? 지금 이 과정이 반드시 빛날 날이 와요 🌙";
            };
            case "20대 후반~30대 초반" -> switch (representativeEmotion) {
                case 100 -> "신입생활 잘 적응하고 있군요! 앞으로가 더 기대돼요 ✨";
                case 75  -> "직장생활 조금씩 익숙해지고 있죠? 잘 하고 있어요 😊";
                case 50  -> "새로운 환경 적응하느라 고생 많아요. 조금만 더 버텨요 💪";
                case 25  -> "회사일이 많이 버겁죠? 퇴근 후 나만의 시간 꼭 가져요 🌱";
                default  -> "신입의 고충 정말 힘들죠. 오늘 하루 정말 수고했어요 🌙";
            };
            case "30대 중반 이상" -> switch (representativeEmotion) {
                case 100 -> "일도 가정도 잘 챙기고 있군요! 정말 대단해요 ✨";
                case 75  -> "바쁜 하루였지만 잘 해냈어요! 오늘도 수고했어요 😊";
                case 50  -> "회사와 가정 사이에서 균형 잡기 쉽지 않죠 💪";
                case 25  -> "많은 걸 짊어지고 있죠. 가끔은 나를 위한 시간도 필요해요 🌱";
                default  -> "정말 많이 지치셨죠? 오늘은 모든 걸 내려놓고 쉬어요 🌙";
            };
            default -> switch (representativeEmotion) {
                case 100 -> "오늘 청춘들 에너지 넘쳐요! ✨";
                case 75  -> "오늘 꽤 좋은 하루였군요! 😊";
                case 50  -> "오늘의 작은 노력이 큰 변화를 만들어요 💪";
                case 25  -> "힘든 하루였나요? 내일은 더 나을 거예요 🌱";
                default  -> "많이 지치셨죠? 오늘 푹 쉬어요 🌙";
            };
        };
    }

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

    // GET /api/diaries/today-mood - 홈 화면 감정 통계 (연령층별)
    public TodayMoodResponseDto getTodayMood(Member member) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.toLocalDate().atTime(21, 0, 0);

        if (now.isAfter(end)) {
            end = end.plusDays(1);
        }
        LocalDateTime start = end.minusDays(1);

        String ageGroup = getAgeGroup(member.getAge());

        List<Diary> diaries;
        if ("전체".equals(ageGroup)) {
            diaries = diaryRepository.findByCreatedAtBetweenAndEmotionIsNotNull(start, end);
        } else {
            diaries = diaryRepository.findByCreatedAtBetweenAndEmotionIsNotNullAndAgeGroup(
                    start, end, getMinAge(ageGroup), getMaxAge(ageGroup)
            );
        }

        if (diaries.isEmpty()) {
            return new TodayMoodResponseDto(
                    0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    50,
                    ageGroup,
                    getMoodMessage(ageGroup, 50),
                    start,
                    end
            );
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
                ageGroup,
                getMoodMessage(ageGroup, representativeEmotion),
                start,
                end
        );
    }

    // 연령층별 최소 나이
    private int getMinAge(String ageGroup) {
        return switch (ageGroup) {
            case "고등학생" -> 0;
            case "20대 초반" -> 20;
            case "20대 중반" -> 24;
            case "20대 후반~30대 초반" -> 27;
            case "30대 중반 이상" -> 34;
            default -> 0;
        };
    }

    // 연령층별 최대 나이
    private int getMaxAge(String ageGroup) {
        return switch (ageGroup) {
            case "고등학생" -> 19;
            case "20대 초반" -> 23;
            case "20대 중반" -> 26;
            case "20대 후반~30대 초반" -> 33;
            case "30대 중반 이상" -> 200;
            default -> 200;
        };
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

        // 댓글 먼저 삭제
        commentRepository.deleteByDiary(diary);

        // 공감 먼저 삭제
        empathyRepository.deleteByDiary(diary);

        // 알림 먼저 삭제
        notificationRepository.deleteByDiaryId(diaryId);

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