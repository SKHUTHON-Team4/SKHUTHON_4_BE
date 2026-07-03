package com.skhuthon.team4.global.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BadWordFilter {

    @Value("${openai.api-key}")
    private String openAiApiKey;

    private static final List<String> BAD_WORDS = List.of(
            "씨발", "개새끼", "지랄", "병신", "미친놈", "꺼져",
            "죽어", "자살", "살인", "강간", "성폭행",
            "스팸", "광고", "홍보", "클릭", "구매"
    );

    public boolean containsBadWord(String content) {
        if (content == null) return false;

        // 1차: 로컬 단어 사전 검사
        String lower = content.toLowerCase();
        boolean hasLocalBadWord = BAD_WORDS.stream().anyMatch(lower::contains);
        if (hasLocalBadWord) return true;

        // 2차: OpenAI Moderation API 검사
        try {
            return checkWithOpenAI(content);
        } catch (Exception e) {
            log.warn("OpenAI Moderation API 호출 실패, 로컬 필터만 사용: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkWithOpenAI(String content) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, String> body = Map.of("input", content);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/moderations",
                request,
                Map.class
        );

        if (response.getBody() == null) return false;

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
        if (results == null || results.isEmpty()) return false;

        Map<String, Object> result = results.get(0);
        Boolean flagged = (Boolean) result.get("flagged");

        if (Boolean.TRUE.equals(flagged)) {
            log.warn("OpenAI Moderation API: 부적절한 내용 감지 - {}", content.substring(0, Math.min(50, content.length())));
        }

        return Boolean.TRUE.equals(flagged);
    }

    public String filter(String content) {
        if (content == null) return null;
        String result = content;
        for (String word : BAD_WORDS) {
            result = result.replace(word, "*".repeat(word.length()));
        }
        return result;
    }
}