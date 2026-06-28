package com.skhuthon.team4.notification;

import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiaryReminderScheduler {

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final EmailService emailService;

    // 매일 22시 - 일기 미작성 유저 알림 (현재 비활성화)
//    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void sendDiaryReminder() {
        log.info("일기 알림 스케줄러 시작");

        LocalDate today = LocalDate.now();

        // 밤 10시 알림 ON인 유저만 조회
        List<Member> members = memberRepository.findAllByIsNotificationNightTrue();

        for (Member member : members) {
            boolean wroteToday = diaryRepository.existsByMemberAndDiaryDate(member, today);
            if (!wroteToday && member.getEmail() != null) {
                emailService.sendDiaryReminder(member.getEmail(), member.getNickname());
            }
        }

        log.info("일기 알림 스케줄러 완료");
    }

    // 매일 오전 8시 30분 - AI 멘트 이메일 발송 (현재 비활성화)
//    @Scheduled(cron = "0 30 8 * * *", zone = "Asia/Seoul")
    public void sendAiCommentEmail() {
        log.info("AI 멘트 이메일 스케줄러 시작");

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