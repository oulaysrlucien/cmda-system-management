package org.cmda.management.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_service_assignments")
public class MemberServiceAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private CmdaMember member;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_id") private CommunityService service;
    @Column(nullable = false) private LocalDate startDate;
    private LocalDate endDate;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public CmdaMember getMember() { return member; }
    public void setMember(CmdaMember member) { this.member = member; }
    public CommunityService getService() { return service; }
    public void setService(CommunityService service) { this.service = service; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
