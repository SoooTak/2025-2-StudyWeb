package com.studyhub.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  // APPROVED | REJECTED | EXPELLED | ROLE_CHANGED | SCHEDULE_CREATED
  @Column(nullable = false, length = 32)
  private String type;

  @Column(name = "study_id")
  private Long studyId; // 컨텍스트(선택)

  @Column(length = 120)
  private String title; // 간단 제목

  @Column(length = 500)
  private String message; // 상세 메시지

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "read_at")
  private LocalDateTime readAt;

  // 보존기간: 생성 후 15일
  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt = LocalDateTime.now().plusDays(15);

  // ===== getters/setters =====
  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public Long getStudyId() { return studyId; }
  public void setStudyId(Long studyId) { this.studyId = studyId; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public LocalDateTime getReadAt() { return readAt; }
  public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
  public LocalDateTime getExpiresAt() { return expiresAt; }
  public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
