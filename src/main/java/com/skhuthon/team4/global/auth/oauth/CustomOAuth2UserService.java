package com.skhuthon.team4.global.auth.oauth;

import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private static final List<String> PROFILE_IMAGES = List.of(
            "bear_01", "bear_02", "bear_03", "bear_04", "bear_05",
            "bear_06", "bear_07", "bear_08", "bear_09"
    );

    private String getRandomProfileImage() {
        return PROFILE_IMAGES.get(new Random().nextInt(PROFILE_IMAGES.size()));
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long kakaoId = (Long) attributes.get("id");

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = (String) kakaoProfile.get("nickname");
        String email = kakaoAccount.containsKey("email")
                ? (String) kakaoAccount.get("email")
                : null;

        Member member = memberRepository.findById(kakaoId)
                .orElse(null);

        if (member == null) {
            // 신규 회원 - 랜덤 프로필 이미지 배정
            member = Member.builder()
                    .id(kakaoId)
                    .nickname(nickname)
                    .profileImage(getRandomProfileImage())
                    .email(email)
                    .isNotification(true)
                    .build();
            memberRepository.save(member);
            log.info("신규 회원 가입 - kakaoId: {}, profileImage: {}", kakaoId, member.getProfileImage());
        }
        // 기존 회원은 저장된 닉네임 유지 (업데이트 없음)

        return new CustomOAuth2User(member, attributes);
    }
}