package com.studyhub.study;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.studyhub.domain.entity.Application;
import com.studyhub.domain.repository.ApplicationRepository;

@RestController
@RequestMapping("/api/admin")
public class LeaderConsoleApi {

  private final ApplicationRepository appRepo;

  public LeaderConsoleApi(ApplicationRepository appRepo) {
    this.appRepo = appRepo;
  }

  /** (리더/공동리더 전용) 대기중 신청 목록 */
  @PreAuthorize("@authz.canManageStudy(#studyId)")
  @GetMapping("/pending")
  public Map<String, Object> pending(@RequestParam Long studyId) {
    List<Application> apps =
        appRepo.findByStudyIdAndStatusOrderByCreatedAtAsc(studyId, "PENDING");
    return Map.of("items", apps);
  }
}
