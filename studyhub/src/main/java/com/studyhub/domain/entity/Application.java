package com.studyhub.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application")
public class Application {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long studyId;

  @Column(nullable = false)
  private Long userId; // 로그인 붙이기 전까지는 데모로 고정 userId 씁니다

  @Column(nullable = false, length = 12)
  private String status = "PENDING"; // PENDING/APPROVED/REJECTED/CANCELLED

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  // --- getter/setter ---
  public Long getId() { return id; }
  public Long getStudyId() { return studyId; }
  public void setStudyId(Long studyId) { this.studyId = studyId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
