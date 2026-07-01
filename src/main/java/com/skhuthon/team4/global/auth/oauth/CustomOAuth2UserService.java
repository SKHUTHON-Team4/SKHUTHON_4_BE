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

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long kakaoId = (Long) attributes.get("id");

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = (String) kakaoProfile.get("nickname");
        String profileImage = (String) kakaoProfile.get("profile_image_url");
        String email = kakaoAccount.containsKey("email")
                ? (String) kakaoAccount.get("email")
                : null;

        Member member = memberRepository.findById(kakaoId)
                .orElse(null);

        if (member == null) {
            // 신규 회원만 카카오 닉네임으로 초기화
            member = Member.builder()
                    .id(kakaoId)
                    .nickname(nickname)
                    .profileImage(profileImage)
                    .email(email)
                    .isNotification(true)
                    .build();
            memberRepository.save(member);
        }
        // 기존 회원은 저장된 닉네임 유지 (업데이트 없음)

        return new CustomOAuth2User(member, attributes);
    }
}