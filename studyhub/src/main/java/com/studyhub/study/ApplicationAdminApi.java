package com.studyhub.study;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationAdminApi {

  private final ApplicationService service;

  public ApplicationAdminApi(ApplicationService service) {
    this.service = service;
  }

  // 리더(임시) 승인
  @PostMapping("/{appId}/approve")
  public ResponseEntity<?> approve(@PathVariable Long appId) {
    try {
      String result = service.approve(appId, 1L); // actorId=1 리더 가정
      return ResponseEntity.ok(Map.of("status", result));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(Map.of("code","NOT_FOUND","message", e.getMessage()));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(Map.of("code","CONFLICT","message", e.getMessage()));
    }
  }

  // 리더(임시) 거절
  @PostMapping("/{appId}/reject")
  public ResponseEntity<?> reject(@PathVariable Long appId) {
    try {
      String result = service.reject(appId, 1L);
      return ResponseEntity.ok(Map.of("status", result));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(Map.of("code","NOT_FOUND","message", e.getMessage()));
    }
  }
}
