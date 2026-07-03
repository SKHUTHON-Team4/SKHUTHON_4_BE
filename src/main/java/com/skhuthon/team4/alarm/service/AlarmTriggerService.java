package com.skhuthon.team4.alarm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skhuthon.team4.alarm.domain.AlarmTrigger;
import com.skhuthon.team4.alarm.domain.repository.AlarmTriggerRepository;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.notification.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmTriggerService {

    private final AlarmTriggerRepository alarmTriggerRepository;
    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    private static final String RECALL_ALARM_URL = "https://skhuthon-ai-api.onrender.com";

    // 일기 작성 시 트리거 추출
    @Transactional
    public void extractTriggers(Member member, String diaryText, LocalDate writtenDate) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "user_id", String.valueOf(member.getId()),
                    "diary_text", diaryText,
                    "written_date", writtenDate.toString(),
                    "age", member.getAge() != null ? member.getAge() : 25
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Object> response = restTemplate.postForEntity(
                    RECALL_ALARM_URL + "/recall-alarm/extract",
                    request,
                    Object.class
            );

            if (response.getBody() != null) {
                String triggerData = objectMapper.writeValueAsString(response.getBody());

                AlarmTrigger trigger = AlarmTrigger.builder()
                        .member(member)
                        .triggerData(triggerData)
                        .diaryExcerpt(diaryText.length() > 100 ? diaryText.substring(0, 100) : diaryText)
                        .triggerDate(writtenDate.plusDays(7)) // 7일 후 알람
                        .build();

                alarmTriggerRepository.save(trigger);
                log.info("알람 트리거 저장 완료 - memberId: {}", member.getId());
            }
        } catch (Exception e) {
            log.warn("알람 트리거 추출 실패 (무시): {}", e.getMessage());
        }
    }

    // 스케줄러에서 오늘 트리거 발송
    @Transactional
    public void sendTodayTriggers() {
        List<AlarmTrigger> triggers = alarmTriggerRepository
                .findByTriggerDateAndIsSentFalse(LocalDate.now());

        for (AlarmTrigger trigger : triggers) {
            try {
                Member member = trigger.getMember();
                if (member.getFcmToken() == null) continue;

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> body = Map.of(
                        "trigger", objectMapper.readValue(trigger.getTriggerData(), Object.class),
                        "age", member.getAge() != null ? member.getAge() : 25,
                        "diary_excerpt", trigger.getDiaryExcerpt()
                );

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(
                        RECALL_ALARM_URL + "/recall-alarm/compose",
                        request,
                        Map.class
                );

                if (response.getBody() != null) {
                    String title = (String) response.getBody().get("title");
                    String bodyText = (String) response.getBody().get("body");

                    fcmService.sendPushNotification(member.getFcmToken(), title, bodyText);
                    trigger.markAsSent();
                    log.info("리콜 알람 발송 완료 - memberId: {}", member.getId());
                }
            } catch (Exception e) {
                log.warn("리콜 알람 발송 실패 (무시): {}", e.getMessage());
            }
        }
    }
}