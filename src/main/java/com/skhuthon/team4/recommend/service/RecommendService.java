package com.skhuthon.team4.recommend.service;

import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final DiaryRepository diaryRepository;
    private final RestTemplate restTemplate;

    private static final String REC_SERVER = "http://13.209.41.39:8000";

    // 서버 시작 시 전체 공개 일기 색인
    @PostConstruct
    public void reindex() {
        try {
            List<Diary> diaries = diaryRepository.findAllByIsPublicTrue();
            List<Map<String, Object>> body = diaries.stream()
                    .map(d -> Map.<String, Object>of(
                            "id", d.getId(),
                            "memberId", d.getMember().getId(),
                            "title", d.getTitle(),
                            "content", d.getContent()
                    ))
                    .collect(Collectors.toList());

            Map response = restTemplate.postForObject(REC_SERVER + "/reindex", body, Map.class);
            log.info("추천 서버 색인 완료: {}", response);
        } catch (Exception e) {
            log.warn("추천 서버 색인 실패 (무시): {}", e.getMessage());
        }
    }

    // 일기 생성 시 색인
    public void indexDiary(Diary diary) {
        try {
            Map<String, Object> body = Map.of(
                    "id", diary.getId(),
                    "memberId", diary.getMember().getId(),
                    "title", diary.getTitle(),
                    "content", diary.getContent()
            );
            restTemplate.postForObject(REC_SERVER + "/diaries", body, String.class);
        } catch (Exception e) {
            log.warn("추천 서버 일기 색인 실패 (무시): {}", e.getMessage());
        }
    }

    // 일기 삭제 시 색인 제거
    public void deleteDiaryIndex(Long diaryId) {
        try {
            restTemplate.delete(REC_SERVER + "/diaries/" + diaryId);
        } catch (Exception e) {
            log.warn("추천 서버 일기 삭제 실패 (무시): {}", e.getMessage());
        }
    }

    // 추천 일기 ID 목록 반환
    public List<Long> getRecommendedDiaryIds(List<Diary> recentDiaries) {
        try {
            List<Map<String, Object>> diaryList = recentDiaries.stream()
                    .map(d -> Map.<String, Object>of(
                            "id", d.getId(),
                            "memberId", d.getMember().getId(),
                            "title", d.getTitle(),
                            "content", d.getContent()
                    ))
                    .collect(Collectors.toList());

            Map<String, Object> body = Map.of("diaries", diaryList, "top_k", 5);
            Map response = restTemplate.postForObject(REC_SERVER + "/recommend", body, Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            return results.stream()
                    .map(r -> ((Number) r.get("diary_id")).longValue())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("추천 서버 호출 실패 (무시): {}", e.getMessage());
            return List.of();
        }
    }
}