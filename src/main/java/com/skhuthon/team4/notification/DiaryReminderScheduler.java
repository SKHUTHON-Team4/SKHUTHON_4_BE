package com.skhuthon.team4.notification;

import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.member.domain.repository.MemberRepository;
import com.skhuthon.team4.notification.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.skhuthon.team4.alarm.service.AlarmTriggerService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryReminderScheduler {

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final EmailService emailService;
    private final FcmService fcmService;
    private final AlarmTriggerService alarmTriggerService;

    // 매일 22시 - 일기 미작성 유저 알림
    @Scheduled(cron = "0 58 23 * * *", zone = "Asia/Seoul")
    public void sendDiaryReminder() {
        log.info("일기 알림 스케줄러 시작");

        LocalDate today = LocalDate.now();

        List<Member> members = memberRepository.findAllByIsNotificationNightTrue();

        for (Member member : members) {
            boolean wroteToday = diaryRepository.existsByMemberAndDiaryDate(member, today);
            if (!wroteToday) {
                // 이메일 알림 (밤 이메일 ON인 경우만)
                if (member.getEmail() != null && member.isNotificationNightEmail()) {
                    emailService.sendDiaryReminder(member.getEmail(), member.getNickname());
                }
                // FCM 푸시 알림 (밤 푸시 ON인 경우만)
                if (member.getFcmToken() != null && member.isNotificationNightPush()) {
                    fcmService.sendPushNotification(
                            member.getFcmToken(),
                            "청춘잇다 📝",
                            "오늘 하루를 아직 기록하지 않으셨네요. 하루를 한 줄로 남겨보는 건 어떨까요?"
                    );
                }
            }
        }

        log.info("일기 알림 스케줄러 완료");
    }

    // 매일 08시 30분 - AI 서버 호출 후 AI 멘트 이메일 + FCM 발송
    @Scheduled(cron = "0 58 23 * * *", zone = "Asia/Seoul")
    public void sendAiCommentEmail() {
        log.info("AI 멘트 스케줄러 시작");

        // AI 서버 호출 (어제 일기 분석 + ai_comment 저장)
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForObject("https://skhuthon-ai-api.onrender.com/alarm/run-ai", String.class);
            log.info("AI 서버 호출 완료");

            // AI 분석 완료까지 잠시 대기 (30초)
            Thread.sleep(30000);
        } catch (Exception e) {
            log.error("AI 서버 호출 실패: {}", e.getMessage());
        }

        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 아침 알림 ON인 유저만 조회
        List<Member> members = memberRepository.findAllByIsNotificationMorningTrue();

        for (Member member : members) {
            Optional<Diary> diaryOpt = diaryRepository
                    .findByMemberAndDiaryDateAndAiCommentIsNotNull(member, yesterday);

            if (diaryOpt.isPresent()) {
                Diary diary = diaryOpt.get();

                // 이메일 알림 (아침 이메일 ON인 경우만)
                if (member.getEmail() != null && member.isNotificationMorningEmail()) {
                    emailService.sendAiComment(
                            member.getEmail(),
                            member.getNickname(),
                            diary.getAiComment()
                    );
                }

                // FCM 푸시 알림 (아침 푸시 ON인 경우만)
                if (member.getFcmToken() != null && member.isNotificationMorningPush()) {
                    fcmService.sendPushNotification(
                            member.getFcmToken(),
                            "청춘잇다 💌",
                            diary.getAiComment()
                    );
                }
            }
        }

        log.info("AI 멘트 스케줄러 완료");
    }

    // 매일 08시 30분 - 리콜 알람 발송
    @Scheduled(cron = "0 13 0 * * *", zone = "Asia/Seoul")
    public void sendRecallAlarms() {
        log.info("리콜 알람 스케줄러 시작");
        alarmTriggerService.sendTodayTriggers();
        log.info("리콜 알람 스케줄러 완료");
    }
}