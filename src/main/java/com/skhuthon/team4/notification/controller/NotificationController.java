package com.skhuthon.team4.notification.controller;

import com.skhuthon.team4.global.common.ApiResponseTemplate;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.notification.dto.NotificationResponseDto;
import com.skhuthon.team4.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림 전체 조회
    @GetMapping
    public ApiResponseTemplate<List<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(notificationService.getMyNotifications(member));
    }

    // 읽지 않은 알림 수
    @GetMapping("/unread-count")
    public ApiResponseTemplate<Integer> getUnreadCount(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(notificationService.getUnreadCount(member));
    }

    // 알림 전체 읽음 처리
    @PatchMapping("/read-all")
    public ApiResponseTemplate<Void> markAllAsRead(
            @AuthenticationPrincipal Member member
    ) {
        notificationService.markAllAsRead(member);
        return ApiResponseTemplate.success();
    }
}