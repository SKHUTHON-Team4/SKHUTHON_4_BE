package com.skhuthon.team4.member.controller;

import com.skhuthon.team4.global.common.ApiResponseTemplate;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.member.dto.MemberResponseDto;
import com.skhuthon.team4.member.dto.MemberUpdateAgeRequestDto;
import com.skhuthon.team4.member.dto.MemberUpdateRequestDto;
import com.skhuthon.team4.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.skhuthon.team4.member.dto.MemberProfileDto;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 닉네임 수정
    @PatchMapping("/me/nickname")
    public ApiResponseTemplate<MemberResponseDto> updateNickname(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody MemberUpdateRequestDto request
    ) {
        return ApiResponseTemplate.success(memberService.updateNickname(member, request));
    }

    // 전체 알림 설정 토글
    @PatchMapping("/me/notification")
    public ApiResponseTemplate<MemberResponseDto> toggleNotification(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(memberService.toggleNotification(member));
    }

    // 밤 10시 알림 ON/OFF
    @PatchMapping("/me/notification/night")
    public ApiResponseTemplate<MemberResponseDto> toggleNotificationNight(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(memberService.toggleNotificationNight(member));
    }

    // 아침 8시 30분 알림 ON/OFF
    @PatchMapping("/me/notification/morning")
    public ApiResponseTemplate<MemberResponseDto> toggleNotificationMorning(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(memberService.toggleNotificationMorning(member));
    }

    // 밤 이메일 알림 ON/OFF
    @PatchMapping("/me/notification/night/email")
    public ApiResponseTemplate<MemberResponseDto> toggleNotificationNightEmail(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(memberService.toggleNotificationNightEmail(member));
    }

    // 밤 앱 푸시 알림 ON/OFF
    @PatchMapping("/me/notification/night/push")
    public ApiResponseTemplate<MemberResponseDto> toggleNotificationNightPush(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(memberService.toggleNotificationNightPush(member));
    }

    // 아침 이메일 알림 ON/OFF
    @PatchMapping("/me/notification/morning/email")
    public ApiResponseTemplate<MemberResponseDto> toggleNotificationMorningEmail(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(memberService.toggleNotificationMorningEmail(member));
    }

    // 아침 앱 푸시 알림 ON/OFF
    @PatchMapping("/me/notification/morning/push")
    public ApiResponseTemplate<MemberResponseDto> toggleNotificationMorningPush(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(memberService.toggleNotificationMorningPush(member));
    }

    // 프로필 통계 조회
    @GetMapping("/me/profile")
    public ApiResponseTemplate<MemberProfileDto> getProfile(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponseTemplate.success(memberService.getProfile(member));
    }

    // 나이 입력
    @PatchMapping("/me/age")
    public ApiResponseTemplate<MemberResponseDto> updateAge(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody MemberUpdateAgeRequestDto request
    ) {
        return ApiResponseTemplate.success(memberService.updateAge(member, request));
    }

    // FCM 토큰 저장
    @PatchMapping("/me/fcm-token")
    public ApiResponseTemplate<Void> updateFcmToken(
            @AuthenticationPrincipal Member member,
            @RequestBody Map<String, String> body
    ) {
        memberService.updateFcmToken(member, body.get("fcmToken"));
        return ApiResponseTemplate.success();
    }

    // 프로필 이미지 변경
    @PatchMapping("/me/profile-image")
    public ApiResponseTemplate<Map<String, String>> updateProfileImage(
            @AuthenticationPrincipal Member member,
            @RequestBody Map<String, String> body
    ) {
        String profileImage = body.get("profileImage");
        memberService.updateProfileImage(member, profileImage);
        return ApiResponseTemplate.success(Map.of("profileImage", profileImage));
    }
}