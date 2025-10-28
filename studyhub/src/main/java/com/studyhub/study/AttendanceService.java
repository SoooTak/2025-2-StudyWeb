package com.studyhub.study;

import com.studyhub.domain.entity.Attendance;
import com.studyhub.domain.entity.StudySession;
import com.studyhub.domain.repository.AttendanceRepository;
import com.studyhub.domain.repository.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AttendanceService {

  private final StudySessionRepository sessionRepo;
  private final AttendanceRepository attendanceRepo;

  public AttendanceService(StudySessionRepository sessionRepo, AttendanceRepository attendanceRepo) {
    this.sessionRepo = sessionRepo;
    this.attendanceRepo = attendanceRepo;
  }

  /**
   * SELF 출석 기록: 규칙
   * - now < startAt + openOffset(-10분) : NOT_OPEN
   * - startAt + openOffset ~ startAt + 30분 : PRESENT
   * - startAt + 30분 ~ endAt : LATE
   * - now >= endAt : CLOSED (버튼 비활성)
   */
  @Transactional
  public Result selfCheck(Long sessionId, Long userId) {
    StudySession ss = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("세션이 없습니다"));

    if (!ss.isUseAttendance() || !"SELF".equalsIgnoreCase(ss.getMode())) {
      throw new IllegalStateException("SELF 출석을 사용할 수 없는 세션입니다");
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime openAt = ss.getStartAt().plusMinutes(ss.getOpenOffsetMinutes()); // -10분
    LocalDateTime presentClose = ss.getStartAt().plusMinutes(30); // +30분
    LocalDateTime endAt = ss.getEndAt();

    if (now.isBefore(openAt)) {
      return new Result("NOT_OPEN", null);
    }
    if (!now.isBefore(endAt)) { // now >= endAt
      return new Result("CLOSED", null);
    }

    String status = now.isBefore(presentClose) || now.isEqual(presentClose) ? "PRESENT" : "LATE";

    Attendance att = attendanceRepo.findBySessionIdAndUserId(sessionId, userId).orElse(null);
    if (att == null) {
      att = new Attendance();
      att.setSessionId(sessionId);
      att.setUserId(userId);
    }
    att.setStatus(status);
    att.setSource("SELF");
    att.setMarkedAt(now);
    att.setUpdatedByUserId(userId);
    attendanceRepo.save(att);

    return new Result(status, att.getId());
  }

  public record Result(String state, Long attendanceId) {}
}
