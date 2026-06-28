package com.skhuthon.team4.member.domain.repository;

import com.skhuthon.team4.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    // 전체 알림 설정 ON인 유저
    List<Member> findAllByIsNotificationTrue();

    // 밤 10시 알림 ON인 유저
    List<Member> findAllByIsNotificationNightTrue();

    // 아침 8시 30분 알림 ON인 유저
    List<Member> findAllByIsNotificationMorningTrue();
}