package com.skhuthon.team4.diary.domain;

import com.skhuthon.team4.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "diaries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "diary_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private LocalDate diaryDate;

    @Column(nullable = false)
    @Builder.Default
    private int empathyCount = 0;

    @Column
    private Integer emotion;  // 100, 75, 50, 25, 0

    @Column(columnDefinition = "TEXT")
    private String aiComment;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPublic = true;

    public void increaseEmpathyCount() {
        this.empathyCount++;
    }

    public void decreaseEmpathyCount() {
        if (this.empathyCount > 0) this.empathyCount--;
    }

    public void updatePublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void update(String title, String content, boolean isPublic) {
        this.title = title;
        this.content = content;
        this.isPublic = isPublic;
    }

    public void updateEmotion(Integer emotion) {
        this.emotion = emotion;
    }

    public void updateAiComment(String aiComment) {
        this.aiComment = aiComment;
    }
}