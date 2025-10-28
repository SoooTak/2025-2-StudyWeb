package com.studyhub.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(
  name = "membership",
  uniqueConstraints = @UniqueConstraint(name = "uk_membership_study_user", columnNames = {"study_id","user_id"})
)
public class Membership {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "study_id", nullable = false)
  private Long studyId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(length = 12, nullable = false)
  private String role = "MEMBER"; // LEADER / CO_LEADER / MEMBER

  // getters/setters
  public Long getId() { return id; }
  public Long getStudyId() { return studyId; }
  public void setStudyId(Long studyId) { this.studyId = studyId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
}
