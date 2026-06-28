package com.skhuthon.team4.empathy.domain.repository;

import com.skhuthon.team4.diary.domain.Diary;
import com.skhuthon.team4.empathy.domain.Empathy;
import com.skhuthon.team4.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpathyRepository extends JpaRepository<Empathy, Long> {

    // 공감 여부 체크
    boolean existsByMemberAndDiary(Member member, Diary diary);

    // 공감 취소용 조회
    Optional<Empathy> findByMemberAndDiary(Member member, Diary diary);

    // 내가 준 공감 수
    int countByMember(Member member);

    // 일기 삭제 시 공감 일괄 삭제
    void deleteByDiary(Diary diary);
}