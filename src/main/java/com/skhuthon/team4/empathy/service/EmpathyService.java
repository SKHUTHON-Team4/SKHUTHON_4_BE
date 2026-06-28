package com.skhuthon.team4.empathy.service;

import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.empathy.domain.Empathy;
import com.skhuthon.team4.empathy.domain.repository.EmpathyRepository;
import com.skhuthon.team4.empathy.dto.EmpathyResponseDto;
import com.skhuthon.team4.global.exception.BusinessException;
import com.skhuthon.team4.global.exception.ErrorCode;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpathyService {

    private final EmpathyRepository empathyRepository;
    private final DiaryRepository diaryRepository;
    private final NotificationService notificationService;

    // POST /api/diaries/{diaryId}/empathy - 공감 토글
    @Transactional
    public EmpathyResponseDto toggleEmpathy(Member member, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        boolean isEmpathized = empathyRepository.existsByMemberAndDiary(member, diary);

        if (isEmpathized) {
            // 공감 취소
            Empathy empathy = empathyRepository.findByMemberAndDiary(member, diary)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EMPATHY_NOT_FOUND));
            empathyRepository.delete(empathy);
            diary.decreaseEmpathyCount();

            return new EmpathyResponseDto(diaryId, diary.getEmpathyCount(), false);
        } else {
            // 공감 추가
            Empathy empathy = Empathy.builder()
                    .member(member)
                    .diary(diary)
                    .build();
            empathyRepository.save(empathy);
            diary.increaseEmpathyCount();

            // 본인 일기가 아닐 때만 알림 저장
            if (!diary.getMember().getId().equals(member.getId())) {
                notificationService.saveNotification(
                        diary.getMember(),
                        "EMPATHY",
                        member.getNickname() + "님이 회원님의 일기에 공감했어요 ❤️",
                        diaryId
                );
            }

            return new EmpathyResponseDto(diaryId, diary.getEmpathyCount(), true);
        }
    }

    // GET /api/diaries/{diaryId}/empathy - 공감 여부 조회
    public EmpathyResponseDto getEmpathyStatus(Member member, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        boolean isEmpathized = empathyRepository.existsByMemberAndDiary(member, diary);

        return new EmpathyResponseDto(diaryId, diary.getEmpathyCount(), isEmpathized);
    }
}