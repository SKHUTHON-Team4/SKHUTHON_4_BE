package com.skhuthon.team4.alarm.domain.repository;

import com.skhuthon.team4.alarm.domain.AlarmTrigger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AlarmTriggerRepository extends JpaRepository<AlarmTrigger, Long> {

    // 오늘 발송해야 할 트리거 조회 (미발송)
    List<AlarmTrigger> findByTriggerDateAndIsSentFalse(LocalDate triggerDate);
}