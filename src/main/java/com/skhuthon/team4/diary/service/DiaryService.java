package com.skhuthon.team4.diary.service;

import com.skhuthon.team4.comment.domain.repository.CommentRepository;
import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.diary.dto.AiCommentRequestDto;
import com.skhuthon.team4.diary.dto.AiDiaryResponseDto;
import com.skhuthon.team4.diary.dto.DiaryRequestDto;
import com.skhuthon.team4.diary.dto.DiaryResponseDto;
import com.skhuthon.team4.diary.dto.RecommendFeedResponseDto;
import com.skhuthon.team4.diary.dto.TodayMoodResponseDto;
import com.skhuthon.team4.empathy.domain.repository.EmpathyRepository;
import com.skhuthon.team4.global.exception.BusinessException;
import com.skhuthon.team4.global.exception.ErrorCode;
import com.skhuthon.team4.global.filter.BadWordFilter;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.notification.domain.repository.NotificationRepository;
import com.skhuthon.team4.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.skhuthon.team4.alarm.service.AlarmTriggerService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final CommentRepository commentRepository;
    private final EmpathyRepository empathyRepository;
    private final NotificationRepository notificationRepository;
    private final BadWordFilter badWordFilter;
    private final RecommendService recommendService;
    private final AlarmTriggerService alarmTriggerService;

    // 연령층 분류
    private String getAgeGroup(Integer age) {
        if (age == null) return "전체";
        if (age <= 19) return "고등학생";
        if (age <= 23) return "20대 초반";
        if (age <= 26) return "20대 중반";
        if (age <= 33) return "20대 후반~30대 초반";
        return "30대 중반 이상";
    }

    // 연령층 + 감정에 따른 메시지 (폴백용)
    private String getMoodMessage(String ageGroup, int representativeEmotion) {
        return switch (ageGroup) {
            case "고등학생" -> switch (representativeEmotion) {
                case 100 -> "오늘 하루 꽤 잘 보낸 친구들이 많네요. 이 기분 오래 기억해둬요 📚";
                case 75  -> "학교생활이 쉽지만은 않은데, 오늘은 그래도 잘 버틴 하루였어요 😊";
                case 50  -> "좋지도 나쁘지도 않은 날도 있어요. 오늘은 잠깐 쉬어가는 날이면 돼요 💙";
                case 25  -> "공부도 관계도 괜히 버겁게 느껴지는 날이죠. 오늘은 스스로를 너무 몰아붙이지 말아요 🌱";
                default  -> "많이 지친 하루였군요. 그래도 오늘을 버텨낸 것만으로도 충분히 대단해요 🌙";
            };
            case "20대 초반" -> switch (representativeEmotion) {
                case 100 -> "스무살 초반의 오늘이 꽤 밝았네요. 이 기분이 앞으로의 힘이 되어줄 거예요 🌸";
                case 75  -> "막막한 날들 사이에서도 오늘은 꽤 괜찮았던 것 같아요. 잘 보내고 있어요 😊";
                case 50  -> "뭘 해야 할지 헷갈리는 날도 자연스러워요. 아직 정해지지 않아서 가능한 게 많은 시기예요 💙";
                case 25  -> "설레기만 할 줄 알았던 시기가 생각보다 무겁죠. 오늘은 조금 천천히 가도 괜찮아요 🌱";
                default  -> "오늘은 많은 마음이 버거웠나 봐요. 혼자 다 감당하려 하지 않아도 돼요 🌙";
            };
            case "20대 중반" -> switch (representativeEmotion) {
                case 100 -> "오늘은 스스로를 조금 믿어도 되는 날이에요. 지금까지 잘 쌓아오고 있었네요 ✨";
                case 75  -> "불안한 와중에도 꽤 잘 버틴 하루예요. 지금처럼만 한 걸음씩 가면 돼요 😊";
                case 50  -> "남들은 빨라 보이고 나만 멈춘 것 같은 날이 있죠. 그래도 방향을 잃지 않으면 괜찮아요 💙";
                case 25  -> "결과가 바로 보이지 않아도, 지금의 시간이 의미 없어지는 건 아니에요 🌱";
                default  -> "오늘은 자존감이 많이 흔들린 날이었군요. 그래도 당신의 가치가 낮아진 건 아니에요 🌙";
            };
            case "20대 후반~30대 초반" -> switch (representativeEmotion) {
                case 100 -> "바쁜 일상 속에서도 좋은 기분을 느낀 하루였네요. 이런 순간들이 오래 남았으면 해요 ✨";
                case 75  -> "정신없는 하루였지만 그래도 잘 지나왔어요. 오늘의 나에게 수고했다고 말해줘요 😊";
                case 50  -> "열심히 사는데도 가끔 허전한 날이 있죠. 그럴 땐 잠깐 멈춰도 괜찮아요 💙";
                case 25  -> "책임져야 할 것들이 많아 버거운 날이었나 봐요. 오늘은 잠깐 내려놔도 돼요 🌱";
                default  -> "많이 지친 하루였군요. 쉬는 것도 내일을 살아가기 위한 중요한 일이에요 🌙";
            };
            case "30대 중반 이상" -> switch (representativeEmotion) {
                case 100 -> "오래 달려온 마음에도 이렇게 가벼운 날이 찾아오네요. 오늘의 여유를 충분히 누려요 😊";
                case 75  -> "힘든 순간도 있었겠지만 잘 마무리한 하루예요. 오늘은 나를 조금 더 다정하게 대해줘요 ☕";
                case 50  -> "쉬어야 하는 걸 알면서도 쉬기 어려운 시기죠. 오늘은 나를 위한 시간을 조금만 남겨둬요 💙";
                case 25  -> "많은 걸 해내느라 마음이 무거웠던 날이네요. 전부 잘하려는 마음을 잠시 내려놔요 🌱";
                default  -> "정말 많이 지친 하루였군요. 지금까지 버텨온 자신을 오늘만큼은 꼭 안아줘요 🌙";
            };
            default -> switch (representativeEmotion) {
                case 100 -> "오늘은 좋은 감정을 느낀 사람이 많았네요. 이 분위기가 내일까지 이어지면 좋겠어요 ✨";
                case 75  -> "나쁘지 않은 하루였어요. 이런 하루들이 모여서 조금씩 힘이 돼요 😊";
                case 50  -> "평범한 하루도 괜찮아요. 오늘을 무사히 지나온 것만으로 충분해요 💙";
                case 25  -> "조금 무거운 하루였군요. 오늘은 스스로에게 너무 엄격하지 않아도 돼요 🌱";
                default  -> "많이 지친 날이었나 봐요. 오늘은 아무것도 잘하려 하지 말고 푹 쉬어요 🌙";
            };
        };
    }

    // AI 서버에서 감정 메시지 생성
    private String getAiMoodMessage(String ageGroup, long totalCount, int count100, int count75,
                                    int count50, int count25, int count0,
                                    long positiveRatio, long negativeRatio, int representativeEmotion) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "ageGroup", ageGroup,
                    "totalCount", totalCount,
                    "count100", count100,
                    "count75", count75,
                    "count50", count50,
                    "count25", count25,
                    "count0", count0,
                    "positiveRatio", positiveRatio,
                    "negativeRatio", negativeRatio,
                    "representativeEmotion", representativeEmotion
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://skhuthon-ai-api.onrender.com/title/generate-mood-message",
                    request,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().get("message") != null) {
                return (String) response.getBody().get("message");
            }
        } catch (Exception e) {
            log.warn("AI 감정 메시지 생성 실패, 폴백 메시지 사용: {}", e.getMessage());
        }

        return getMoodMessage(ageGroup, representativeEmotion);
    }

    // POST /api/diaries - 일기 작성 (하루 1개 제한)
    @Transactional
    public DiaryResponseDto createDiary(Member member, DiaryRequestDto request) {
        LocalDate today = LocalDate.now();

        if (diaryRepository.existsByMemberAndDiaryDate(member, today)) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_EXISTS);
        }

        if (badWordFilter.containsBadWord(request.title())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
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

        if (saved.isPublic()) {
            recommendService.indexDiary(saved);
        }

        // 리콜 알람 트리거 추출
        alarmTriggerService.extractTriggers(member, request.content(), today);

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

    // PATCH /api/diaries/{diaryId}/ai-comment - AI 멘트 단건 저장 (AI팀 호출용)
    @Transactional
    public DiaryResponseDto updateAiComment(Long diaryId, String aiComment) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        diary.updateAiComment(aiComment);
        return DiaryResponseDto.from(diary, commentRepository.countByDiary(diary));
    }

    // GET /api/diaries/today/public - 오늘 공개 일기 조회 (AI팀용)
    public List<AiDiaryResponseDto> getTodayPublicDiaries() {
        return diaryRepository.findTodayPublicDiaries(LocalDate.now())
                .stream()
                .map(d -> new AiDiaryResponseDto(
                        d.getId(),
                        d.getMember().getAge(),
                        d.getContent(),
                        d.isPublic()
                ))
                .toList();
    }

    // POST /api/diaries/ai-comments - AI 멘트 일괄 저장 (AI팀용)
    @Transactional
    public void updateAiComments(AiCommentRequestDto request) {
        for (AiCommentRequestDto.Recommendation rec : request.recommendations()) {
            diaryRepository.findById(rec.id()).ifPresent(diary -> {
                diary.updateAiComment(rec.aiComment());
            });
        }
    }

    // GET /api/diaries/today-mood - 홈 화면 감정 통계 (연령층별)
    public TodayMoodResponseDto getTodayMood(Member member) {
        // 나이 미입력 시 에러
        if (member.getAge() == null) {
            throw new BusinessException(ErrorCode.AGE_REQUIRED);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.toLocalDate().atTime(21, 0, 0);

        if (now.isAfter(end)) {
            end = end.plusDays(1);
        }
        LocalDateTime start = end.minusDays(1);

        String ageGroup = getAgeGroup(member.getAge());

        List<Diary> diaries = diaryRepository.findByCreatedAtBetweenAndEmotionIsNotNullAndAgeGroup(
                start, end, getMinAge(ageGroup), getMaxAge(ageGroup)
        );

        if (diaries.isEmpty()) {
            return new TodayMoodResponseDto(
                    0, 0, 0, 0, 0, 0,
                    50, 50,
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

        long positiveRatio = Math.round((count100 + count75 + count50 * 0.5) * 100.0 / total);
        long negativeRatio = 100 - positiveRatio;

        int representativeEmotion;
        if (positiveRatio >= 75) representativeEmotion = 100;
        else if (positiveRatio > negativeRatio) representativeEmotion = 75;
        else if (negativeRatio >= 75) representativeEmotion = 0;
        else if (negativeRatio > positiveRatio) representativeEmotion = 25;
        else representativeEmotion = 50;

        // AI 서버에서 메시지 생성 (실패 시 하드코딩 폴백)
        String moodMessage = getAiMoodMessage(
                ageGroup, total, count100, count75, count50, count25, count0,
                positiveRatio, negativeRatio, representativeEmotion
        );

        return new TodayMoodResponseDto(
                total,
                count100, count75, count50, count25, count0,
                positiveRatio,
                negativeRatio,
                representativeEmotion,
                ageGroup,
                moodMessage,
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

    // 추천 피드 (내 일기 5개 이상일 때만 추천)
    public RecommendFeedResponseDto getRecommendFeed(Member member) {
        List<Diary> recentDiaries = diaryRepository.findTop5ByMemberOrderByCreatedAtDesc(member);

        if (recentDiaries.size() < 5) {
            return new RecommendFeedResponseDto(
                    List.of(),
                    "아직 내 일기가 많지 않아요. 일기를 더 작성하면 맞춤 추천을 받을 수 있어요 ✍️"
            );
        }

        List<Long> recommendedIds = recommendService.getRecommendedDiaryIds(recentDiaries);

        if (recommendedIds.isEmpty()) {
            return new RecommendFeedResponseDto(
                    List.of(),
                    "아직 내 일기가 많지 않아요. 일기를 더 작성하면 맞춤 추천을 받을 수 있어요 ✍️"
            );
        }

        List<DiaryResponseDto> diaries = recommendedIds.stream()
                .map(id -> diaryRepository.findById(id).orElse(null))
                .filter(d -> d != null)
                .map(d -> DiaryResponseDto.from(d, commentRepository.countByDiary(d)))
                .toList();

        return new RecommendFeedResponseDto(diaries, null);
    }

    // 핫 피드 (이번 주 기준)
    public List<DiaryResponseDto> getHotFeed() {
        LocalDateTime startOfWeek = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atStartOfDay();
        LocalDateTime endOfWeek = LocalDate.now()
                .with(DayOfWeek.SUNDAY)
                .atTime(23, 59, 59);

        return diaryRepository.findTop10ThisWeekByEmpathyCount(startOfWeek, endOfWeek)
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

        commentRepository.deleteByDiary(diary);
        empathyRepository.deleteByDiary(diary);
        notificationRepository.deleteByDiaryId(diaryId);
        recommendService.deleteDiaryIndex(diaryId);
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

        if (badWordFilter.containsBadWord(request.title())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
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