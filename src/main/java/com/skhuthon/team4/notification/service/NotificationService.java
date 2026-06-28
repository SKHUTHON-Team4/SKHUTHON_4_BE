package com.skhuthon.team4.notification.service;

import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.notification.domain.Notification;
import com.skhuthon.team4.notification.domain.repository.NotificationRepository;
import com.skhuthon.team4.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 알림 저장 (공감/댓글 발생 시 호출)
    @Transactional
    public void saveNotification(Member member, String type, String message, Long diaryId) {
        Notification notification = Notification.builder()
                .member(member)
                .type(type)
                .message(message)
                .diaryId(diaryId)
                .build();
        notificationRepository.save(notification);
    }

    // 내 알림 전체 조회
    public List<NotificationResponseDto> getMyNotifications(Member member) {
        return notificationRepository.findByMemberOrderByCreatedAtDesc(member)
                .stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    // 읽지 않은 알림 수
    public int getUnreadCount(Member member) {
        return notificationRepository.countByMemberAndIsReadFalse(member);
    }

    // 알림 읽음 처리
    @Transactional
    public void markAllAsRead(Member member) {
        notificationRepository.findByMemberOrderByCreatedAtDesc(member)
                .forEach(Notification::markAsRead);
    }
}