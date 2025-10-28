package com.studyhub.study;

import com.studyhub.domain.entity.Application;
import com.studyhub.domain.repository.ApplicationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ApplicationApi {

  private final ApplicationRepository appRepo;
  public ApplicationApi(ApplicationRepository appRepo) { this.appRepo = appRepo; }

  // 로그인 전: 테스트용 고정 유저 ID
  private static final long DEMO_USER_ID = 2L;

  // 신청하기
  @PostMapping("/api/studies/{id}/apply")
  public ResponseEntity<?> apply(@PathVariable Long id) {
    // 같은 스터디에 PENDING 중복 방지(최소 검증)
    var dup = appRepo.findByStudyIdAndUserIdAndStatus(id, DEMO_USER_ID, "PENDING");
    if (dup.isPresent()) {
      return ResponseEntity.badRequest().body(Map.of(
        "code","ALREADY_PENDING",
        "message","이미 신청 대기중입니다",
        "applicationId", dup.get().getId()
      ));
    }
    var app = new Application();
    app.setStudyId(id);
    app.setUserId(DEMO_USER_ID);
    appRepo.save(app);
    return ResponseEntity.status(201).body(Map.of(
      "applicationId", app.getId(),
      "status", app.getStatus()
    ));
  }

  // 신청 취소
  @DeleteMapping("/api/applications/{appId}")
  public ResponseEntity<?> cancel(@PathVariable Long appId) {
    var app = appRepo.findById(appId).orElse(null);
    if (app == null || !app.getUserId().equals(DEMO_USER_ID)) {
      return ResponseEntity.status(404).body(Map.of("code","NOT_FOUND","message","신청이 없습니다"));
    }
    app.setStatus("CANCELLED");
    appRepo.save(app);
    return ResponseEntity.noContent().build();
  }

  // 특정 스터디에 대해 내 신청상태 조회(버튼 표시용)
  @GetMapping("/api/studies/{id}/my-application")
  public Map<String, Object> myApplication(@PathVariable Long id) {
    var pending = appRepo.findByStudyIdAndUserIdAndStatus(id, DEMO_USER_ID, "PENDING");
    if (pending.isPresent()) {
      return Map.of("hasPending", true, "applicationId", pending.get().getId());
    }
    return Map.of("hasPending", false);
  }
}
