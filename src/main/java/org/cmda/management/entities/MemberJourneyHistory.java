package org.cmda.management.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_journey_history")
public class MemberJourneyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private CmdaMember member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journey_stage_id", nullable = false)
    private JourneyStage journeyStage;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Long createdByUserId;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Long updatedByUserId;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public CmdaMember getMember() {
        return member;
    }

    public void setMember(CmdaMember member) {
        this.member = member;
    }

    public JourneyStage getJourneyStage() {
        return journeyStage;
    }

    public void setJourneyStage(JourneyStage journeyStage) {
        this.journeyStage = journeyStage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }
}
