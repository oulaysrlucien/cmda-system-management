package org.cmda.management.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_responsibilities")
public class MemberResponsibility {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private CmdaMember member;
    @Column(nullable = false, length = 160) private String title;
    @Column(length = 160) private String contextLabel;
    @Column(length = 1000) private String description;
    @Column(nullable = false) private LocalDate startDate;
    private LocalDate endDate;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public CmdaMember getMember() { return member; }
    public void setMember(CmdaMember member) { this.member = member; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContextLabel() { return contextLabel; }
    public void setContextLabel(String contextLabel) { this.contextLabel = contextLabel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
