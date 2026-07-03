package com.skhuthon.team4.notification;

import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    // 매일 22시 - 일기 미작성 유저 알림
    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void sendDiaryReminder() {
        log.info("일기 알림 스케줄러 시작");

        LocalDate today = LocalDate.now();

        List<Member> members = memberRepository.findAllByIsNotificationNightTrue();

        for (Member member : members) {
            boolean wroteToday = diaryRepository.existsByMemberAndDiaryDate(member, today);
            if (!wroteToday && member.getEmail() != null) {
                emailService.sendDiaryReminder(member.getEmail(), member.getNickname());
            }
        }

        log.info("일기 알림 스케줄러 완료");
    }

    // 매일 08시 30분 - AI 서버 호출 후 AI 멘트 이메일 발송
    @Scheduled(cron = "0 30 8 * * *", zone = "Asia/Seoul")
    public void sendAiCommentEmail() {
        log.info("AI 멘트 이메일 스케줄러 시작");

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
            if (member.getEmail() == null) continue;

            Optional<Diary> diaryOpt = diaryRepository
                    .findByMemberAndDiaryDateAndAiCommentIsNotNull(member, yesterday);

            if (diaryOpt.isPresent()) {
                Diary diary = diaryOpt.get();
                emailService.sendAiComment(
                        member.getEmail(),
                        member.getNickname(),
                        diary.getAiComment()
                );
            }
        }

        log.info("AI 멘트 이메일 스케줄러 완료");
    }
}