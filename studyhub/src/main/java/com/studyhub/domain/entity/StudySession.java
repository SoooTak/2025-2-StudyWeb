package com.studyhub.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_session")
public class StudySession {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "study_id", nullable = false)
  private Long studyId;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(name = "start_at", nullable = false)
  private LocalDateTime startAt;

  @Column(name = "end_at", nullable = false)
  private LocalDateTime endAt;

  // 출석 사용 여부
  @Column(name = "use_attendance", nullable = false)
  private boolean useAttendance = true;

  // SELF | LEADER_ONLY
  @Column(nullable = false, length = 20)
  private String mode = "SELF";

  // SELF 기본 규칙: 시작 -10분 ~ +30분 = 출석, +30분 ~ 종료 = 지각
  @Column(name = "open_offset_minutes", nullable = false)
  private int openOffsetMinutes = -10;

  @Column(name = "close_offset_minutes", nullable = false)
  private int closeOffsetMinutes = 30;

  // ===== getters/setters =====
  public Long getId() { return id; }
  public Long getStudyId() { return studyId; }
  public void setStudyId(Long studyId) { this.studyId = studyId; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public LocalDateTime getStartAt() { return startAt; }
  public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
  public LocalDateTime getEndAt() { return endAt; }
  public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
  public boolean isUseAttendance() { return useAttendance; }
  public void setUseAttendance(boolean useAttendance) { this.useAttendance = useAttendance; }
  public String getMode() { return mode; }
  public void setMode(String mode) { this.mode = mode; }
  public int getOpenOffsetMinutes() { return openOffsetMinutes; }
  public void setOpenOffsetMinutes(int openOffsetMinutes) { this.openOffsetMinutes = openOffsetMinutes; }
  public int getCloseOffsetMinutes() { return closeOffsetMinutes; }
  public void setCloseOffsetMinutes(int closeOffsetMinutes) { this.closeOffsetMinutes = closeOffsetMinutes; }
}
