package com.skhuthon.team4.alarm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skhuthon.team4.alarm.domain.AlarmTrigger;
import com.skhuthon.team4.alarm.domain.repository.AlarmTriggerRepository;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.notification.EmailService;
import com.skhuthon.team4.notification.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmTriggerService {

    private final AlarmTriggerRepository alarmTriggerRepository;
    private final FcmService fcmService;
    private final EmailService emailService;
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

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    RECALL_ALARM_URL + "/recall-alarm/extract",
                    request,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> triggers = (List<Map<String, Object>>) responseBody.get("triggers");

                if (triggers != null && !triggers.isEmpty()) {
                    for (Map<String, Object> triggerItem : triggers) {
                        // target_date 추출
                        String targetDateStr = (String) triggerItem.get("target_date");
                        LocalDate triggerDate = targetDateStr != null
                                ? LocalDate.parse(targetDateStr)
                                : writtenDate.plusDays(7);

                        // alarm_time 추출 (예: "15:00")
                        String alarmTimeStr = (String) triggerItem.get("alarm_time");
                        LocalTime triggerTime = null;
                        if (alarmTimeStr != null && alarmTimeStr.contains(":")) {
                            try {
                                triggerTime = LocalTime.parse(alarmTimeStr);
                            } catch (Exception e) {
                                log.warn("alarm_time 파싱 실패: {}", alarmTimeStr);
                            }
                        }

                        String triggerData = objectMapper.writeValueAsString(triggerItem);

                        AlarmTrigger trigger = AlarmTrigger.builder()
                                .member(member)
                                .triggerData(triggerData)
                                .diaryExcerpt(diaryText.length() > 100 ? diaryText.substring(0, 100) : diaryText)
                                .triggerDate(triggerDate)
                                .triggerTime(triggerTime)
                                .build();

                        alarmTriggerRepository.save(trigger);
                        log.info("알람 트리거 저장 완료 - memberId: {}, triggerDate: {}, triggerTime: {}", member.getId(), triggerDate, triggerTime);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("알람 트리거 추출 실패 (무시): {}", e.getMessage());
        }
    }

    // 스케줄러에서 오늘 트리거 발송
    @Transactional
    public void sendTodayTriggers() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<AlarmTrigger> triggers = alarmTriggerRepository
                .findByTriggerDateAndIsSentFalse(today);

        for (AlarmTrigger trigger : triggers) {
            try {
                // trigger_time이 있으면 현재 시간과 비교 (±5분 오차 허용)
                if (trigger.getTriggerTime() != null) {
                    LocalTime triggerTime = trigger.getTriggerTime();
                    if (now.isBefore(triggerTime.minusMinutes(5)) ||
                            now.isAfter(triggerTime.plusMinutes(5))) {
                        continue; // 아직 발송 시간 아님
                    }
                }

                Member member = trigger.getMember();

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

                    // FCM 푸시 알림 발송
                    if (member.getFcmToken() != null) {
                        fcmService.sendPushNotification(member.getFcmToken(), title, bodyText);
                    }

                    // 이메일 발송
                    if (member.getEmail() != null) {
                        emailService.sendAiComment(member.getEmail(), member.getNickname(), bodyText);
                    }

                    trigger.markAsSent();
                    log.info("리콜 알람 발송 완료 - memberId: {}", member.getId());
                }
            } catch (Exception e) {
                log.warn("리콜 알람 발송 실패 (무시): {}", e.getMessage());
            }
        }
    }
}