package com.skhuthon.team4.member.service;

import com.skhuthon.team4.global.exception.BusinessException;
import com.skhuthon.team4.global.exception.ErrorCode;
import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.member.domain.repository.MemberRepository;
import com.skhuthon.team4.member.dto.MemberResponseDto;
import com.skhuthon.team4.member.dto.MemberUpdateAgeRequestDto;
import com.skhuthon.team4.member.dto.MemberUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.skhuthon.team4.diary.domain.repository.DiaryRepository;
import com.skhuthon.team4.empathy.domain.repository.EmpathyRepository;
import com.skhuthon.team4.member.dto.MemberProfileDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final EmpathyRepository empathyRepository;

    // PATCH /api/members/me/nickname - 닉네임 수정
    @Transactional
    public MemberResponseDto updateNickname(Member member, MemberUpdateRequestDto request) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        findMember.updateNickname(request.nickname());

        return MemberResponseDto.from(findMember);
    }

    // PATCH /api/members/me/notification - 전체 알림 설정 토글
    @Transactional
    public MemberResponseDto toggleNotification(Member member) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        findMember.updateNotification(!findMember.isNotification());

        return MemberResponseDto.from(findMember);
    }

    // PATCH /api/members/me/notification/night - 밤 알림 ON/OFF
    @Transactional
    public MemberResponseDto toggleNotificationNight(Member member) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        findMember.updateNotificationNight(!findMember.isNotificationNight());

        return MemberResponseDto.from(findMember);
    }

    // PATCH /api/members/me/notification/morning - 아침 알림 ON/OFF
    @Transactional
    public MemberResponseDto toggleNotificationMorning(Member member) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        findMember.updateNotificationMorning(!findMember.isNotificationMorning());

        return MemberResponseDto.from(findMember);
    }

    // GET /api/members/me/profile - 프로필 통계 조회
    public MemberProfileDto getProfile(Member member) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        int diaryCount = diaryRepository.countByMember(findMember);
        int receivedEmpathy = diaryRepository.sumEmpathyCountByMember(findMember);
        int givenEmpathy = empathyRepository.countByMember(findMember);

        return new MemberProfileDto(
                findMember.getId(),
                findMember.getNickname(),
                findMember.getProfileImage(),
                findMember.getEmail(),
                findMember.isNotification(),
                findMember.isNotificationNight(),
                findMember.isNotificationMorning(),
                findMember.isNotificationNightEmail(),
                findMember.isNotificationNightPush(),
                findMember.isNotificationMorningEmail(),
                findMember.isNotificationMorningPush(),
                diaryCount,
                receivedEmpathy,
                givenEmpathy
        );
    }

    @Transactional
    public MemberResponseDto updateAge(Member member, MemberUpdateAgeRequestDto request) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        findMember.updateAge(request.age());

        return MemberResponseDto.from(findMember);
    }

    @Transactional
    public void updateFcmToken(Member member, String fcmToken) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        findMember.updateFcmToken(fcmToken);
    }

    @Transactional
    public MemberResponseDto toggleNotificationNightEmail(Member member) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        findMember.updateNotificationNightEmail(!findMember.isNotificationNightEmail());
        return MemberResponseDto.from(findMember);
    }

    @Transactional
    public MemberResponseDto toggleNotificationNightPush(Member member) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        findMember.updateNotificationNightPush(!findMember.isNotificationNightPush());
        return MemberResponseDto.from(findMember);
    }

    @Transactional
    public MemberResponseDto toggleNotificationMorningEmail(Member member) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        findMember.updateNotificationMorningEmail(!findMember.isNotificationMorningEmail());
        return MemberResponseDto.from(findMember);
    }

    @Transactional
    public MemberResponseDto toggleNotificationMorningPush(Member member) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        findMember.updateNotificationMorningPush(!findMember.isNotificationMorningPush());
        return MemberResponseDto.from(findMember);
    }

    @Transactional
    public void updateProfileImage(Member member, String profileImage) {
        List<String> validImages = List.of(
                "bear_01", "bear_02", "bear_03", "bear_04",
                "bear_05", "bear_06", "bear_07", "bear_08",
                "bear_09", "bear_10", "bear_11", "bear_12",
                "bear_13", "bear_14", "bear_15", "bear_16"
        );
        if (!validImages.contains(profileImage)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        findMember.updateProfile(findMember.getNickname(), profileImage);
    }
}