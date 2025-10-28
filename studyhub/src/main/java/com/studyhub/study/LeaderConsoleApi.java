package com.studyhub.study;

import com.studyhub.domain.entity.Application;
import com.studyhub.domain.repository.ApplicationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class LeaderConsoleApi {

  private final ApplicationRepository appRepo;
  public LeaderConsoleApi(ApplicationRepository appRepo) { this.appRepo = appRepo; }

  @GetMapping("/pending")
  public Map<String, Object> pending(@RequestParam Long studyId) {
    List<Application> apps = appRepo.findByStudyIdAndStatusOrderByCreatedAtAsc(studyId, "PENDING");
    return Map.of("items", apps);
  }
}
