package com.studyhub.study;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.studyhub.common.api.ErrorResponse;
import com.studyhub.domain.entity.Attendance;
import com.studyhub.domain.entity.StudySession;
import com.studyhub.domain.repository.AttendanceRepository;
import com.studyhub.domain.repository.StudySessionRepository;
import com.studyhub.security.AuthUtils;

@RestController
public class SessionApi {

  private final StudySessionRepository sessionRepo;
  private final AttendanceRepository attendanceRepo;
  private final AttendanceService attendanceService;
  private final NotificationService notificationService;

  public SessionApi(StudySessionRepository sessionRepo,
                    AttendanceRepository attendanceRepo,
                    AttendanceService attendanceService,
                    NotificationService notificationService) {
    this.sessionRepo = sessionRepo;
    this.attendanceRepo = attendanceRepo;
    this.attendanceService = attendanceService;
    this.notificationService = notificationService;
  }

  /** 특정 스터디의 세션 목록 */
  @GetMapping("/api/studies/{studyId}/sessions")
  public Map<String, Object> list(@PathVariable Long studyId) {
    List<StudySession> items = sessionRepo.findByStudyIdOrderByStartAtAsc(studyId);
    return Map.of("items", items);
  }

  /** (리더/공동리더) 세션 생성 */
  @PreAuthorize("@authz.canManageStudy(#studyId)")
  @PostMapping("/api/studies/{studyId}/sessions")
  public ResponseEntity<?> create(@PathVariable Long studyId, @Valid @RequestBody CreateReq req) {
    if (req.endAt() != null && req.startAt() != null && !req.endAt().isAfter(req.startAt())) {
      // ✅ 표준 포맷으로 400
      return ResponseEntity.badRequest()
          .body(ErrorResponse.of("BAD_REQUEST", "시간 범위가 올바르지 않습니다"));
    }

    // 생성
    StudySession ss = new StudySession();
    ss.setStudyId(studyId);
    ss.setTitle(req.title());
    ss.setStartAt(req.startAt());
    ss.setEndAt(req.endAt());
    ss.setUseAttendance(req.useAttendance() == null ? true : req.useAttendance());
    ss.setMode(req.mode() == null ? "SELF" : req.mode());
    if (req.openOffsetMinutes() != null) ss.setOpenOffsetMinutes(req.openOffsetMinutes());
    if (req.closeOffsetMinutes() != null) ss.setCloseOffsetMinutes(req.closeOffsetMinutes());
    sessionRepo.save(ss);

    // 알림: 일정 등록 → 스터디 모든 멤버에게
    String when = ss.getStartAt().toString().replace('T',' ');
    notificationService.notifyStudyMembers(
        studyId,
        "SCHEDULE_CREATED",
        "새 일정이 등록되었습니다",
        "일정: " + ss.getTitle() + " (시작 " + when + ")"
    );

    return ResponseEntity.status(201).body(Map.of("id", ss.getId()));
  }

  /** 내 출석 조회(로그인 필요) */
  @GetMapping("/api/sessions/{sessionId}/attendance/my")
  public Map<String, Object> my(@PathVariable Long sessionId) {
    Long userId = AuthUtils.requireUserId();
    Attendance att = attendanceRepo.findBySessionIdAndUserId(sessionId, userId).orElse(null);
    return att == null
        ? Map.of("has", false)
        : Map.of("has", true, "status", att.getStatus(), "markedAt", att.getMarkedAt());
  }

  /** 자기출석(SELF) (로그인 필요) */
  @PostMapping("/api/sessions/{sessionId}/attendance/self")
  public ResponseEntity<?> self(@PathVariable Long sessionId) {
    Long userId = AuthUtils.requireUserId();
    try {
      var res = attendanceService.selfCheck(sessionId, userId);
      return ResponseEntity.ok(Map.of("result", res.state(), "attendanceId", res.attendanceId()));
    } catch (IllegalArgumentException e) {
      // ✅ 표준 포맷으로 404
      return ResponseEntity.status(404).body(ErrorResponse.of("NOT_FOUND", e.getMessage()));
    } catch (IllegalStateException e) {
      // ✅ 표준 포맷으로 409
      return ResponseEntity.status(409).body(ErrorResponse.of("CONFLICT", e.getMessage()));
    }
  }

  public record CreateReq(
      @NotBlank String title,
      @NotNull LocalDateTime startAt,
      @NotNull LocalDateTime endAt,
      Boolean useAttendance,
      String mode,
      Integer openOffsetMinutes,
      Integer closeOffsetMinutes
  ) {}
}
