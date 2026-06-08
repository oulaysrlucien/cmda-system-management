package org.cmda.management.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.cmda.management.enums.MemberStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.cmda.management.configs.WebConfig;

@Entity
@Table(name = "cmda_members")
public class CmdaMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    //@Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private LocalDate baptismDate;

    @Column(nullable = true)
    private String profession;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String talentsAndSkills;

    private String addressLine1;

    private String addressLine2;

    @Column(length = 32)
    private String postalCode;

    @Column(length = 160)
    private String city;

    @Column(length = 160)
    private String administrativeArea;

    @Column(length = 2)
    private String countryCode;

    private LocalDate communityEntryDate;

    private LocalDate definitiveCommitmentDate;

    private String photoReference;

    @Column(length = 2000)
    private String internalNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_journey_stage_id")
    private JourneyStage currentJourneyStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "life_state_id")
    private LifeState lifeState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_reason_id")
    private ArchiveReason archiveReason;

    @Column(length = 1000)
    private String archiveComment;

    private LocalDateTime archivedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraternity_id")
    @JsonBackReference  // Côté enfant de la relation
    //@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "region"})  // Ignore les proxys Hibernate et la région
    private Fraternity fraternity;

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


    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public LocalDate getBaptismDate() {
        return baptismDate;
    }

    public void setBaptismDate(LocalDate baptismDate) {
        this.baptismDate = baptismDate;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getTalentsAndSkills() {
        return talentsAndSkills;
    }

    public void setTalentsAndSkills(String talentsAndSkills) {
        this.talentsAndSkills = talentsAndSkills;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAdministrativeArea() {
        return administrativeArea;
    }

    public void setAdministrativeArea(String administrativeArea) {
        this.administrativeArea = administrativeArea;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public LocalDate getCommunityEntryDate() {
        return communityEntryDate;
    }

    public void setCommunityEntryDate(LocalDate communityEntryDate) {
        this.communityEntryDate = communityEntryDate;
    }

    public LocalDate getDefinitiveCommitmentDate() {
        return definitiveCommitmentDate;
    }

    public void setDefinitiveCommitmentDate(LocalDate definitiveCommitmentDate) {
        this.definitiveCommitmentDate = definitiveCommitmentDate;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public JourneyStage getCurrentJourneyStage() {
        return currentJourneyStage;
    }

    public void setCurrentJourneyStage(JourneyStage currentJourneyStage) {
        this.currentJourneyStage = currentJourneyStage;
    }

    public LifeState getLifeState() {
        return lifeState;
    }

    public void setLifeState(LifeState lifeState) {
        this.lifeState = lifeState;
    }

    public ArchiveReason getArchiveReason() {
        return archiveReason;
    }

    public void setArchiveReason(ArchiveReason archiveReason) {
        this.archiveReason = archiveReason;
    }

    public String getArchiveComment() {
        return archiveComment;
    }

    public void setArchiveComment(String archiveComment) {
        this.archiveComment = archiveComment;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Fraternity getFraternity() {
        return fraternity;
    }

    public void setFraternity(Fraternity fraternity) {
        this.fraternity = fraternity;
    }
}
