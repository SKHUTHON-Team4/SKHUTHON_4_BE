package com.skhuthon.team4.alarm.domain;

import com.skhuthon.team4.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alarm_triggers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlarmTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "trigger_data", nullable = false, columnDefinition = "JSON")
    private String triggerData;

    @Column(name = "diary_excerpt", length = 500)
    private String diaryExcerpt;

    @Column(name = "trigger_date", nullable = false)
    private LocalDate triggerDate;

    @Column(name = "is_sent")
    @Builder.Default
    private boolean isSent = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Column(name = "trigger_time")
    private LocalTime triggerTime;

    public void markAsSent() {
        this.isSent = true;
    }
}