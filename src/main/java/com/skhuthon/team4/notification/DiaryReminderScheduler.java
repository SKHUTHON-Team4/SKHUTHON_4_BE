package com.skhuthon.team4.notification;

import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiaryReminderScheduler {

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final EmailService emailService;

    // 매일 22시 실행
//    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void sendDiaryReminder() {
        log.info("일기 알림 스케줄러 시작");

        LocalDate today = LocalDate.now();

        // 알림 설정 ON인 유저 전체 조회
        List<Member> members = memberRepository.findAllByIsNotificationTrue();

        for (Member member : members) {
            // 오늘 일기 이미 쓴 유저는 제외
            boolean wroteToday = diaryRepository.existsByMemberAndDiaryDate(member, today);
            if (!wroteToday && member.getEmail() != null) {
                emailService.sendDiaryReminder(member.getEmail(), member.getNickname());
            }
        }

        log.info("일기 알림 스케줄러 완료");
    }
}