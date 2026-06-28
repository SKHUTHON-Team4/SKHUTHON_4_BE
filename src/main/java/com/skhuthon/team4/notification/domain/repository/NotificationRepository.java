package com.skhuthon.team4.notification.domain.repository;

import com.skhuthon.team4.member.domain.Member;
import com.skhuthon.team4.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 내 알림 전체 조회 (최신순)
    List<Notification> findByMemberOrderByCreatedAtDesc(Member member);

    // 읽지 않은 알림 수
    int countByMemberAndIsReadFalse(Member member);

    // 일기 삭제 시 알림 일괄 삭제
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.diaryId = :diaryId")
    void deleteByDiaryId(@Param("diaryId") Long diaryId);
}