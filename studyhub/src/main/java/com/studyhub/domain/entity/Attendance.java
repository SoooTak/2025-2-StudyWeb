package com.studyhub.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
  name = "attendance",
  uniqueConstraints = @UniqueConstraint(name = "uk_attendance_session_user", columnNames = {"session_id","user_id"})
)
public class Attendance {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  // PRESENT | LATE | ABSENT
  @Column(nullable = false, length = 12)
  private String status = "PRESENT";

  // SELF | LEADER
  @Column(nullable = false, length = 12)
  private String source = "SELF";

  @Column(name = "marked_at", nullable = false)
  private LocalDateTime markedAt = LocalDateTime.now();

  // (선택) 수정자 기록
  @Column(name = "updated_by_user_id")
  private Long updatedByUserId;

  // ===== getters/setters =====
  public Long getId() { return id; }
  public Long getSessionId() { return sessionId; }
  public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }
  public LocalDateTime getMarkedAt() { return markedAt; }
  public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }
  public Long getUpdatedByUserId() { return updatedByUserId; }
  public void setUpdatedByUserId(Long updatedByUserId) { this.updatedByUserId = updatedByUserId; }
}
