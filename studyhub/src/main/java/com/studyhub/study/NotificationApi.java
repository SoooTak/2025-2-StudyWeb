package com.studyhub.study;

import com.studyhub.domain.entity.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApi {

  private static final long DEMO_USER_ID = 2L; // 로그인 전 임시
  private final NotificationService service;

  public NotificationApi(NotificationService service) { this.service = service; }

  @GetMapping
  public Map<String, Object> list() {
    return Map.of("items", service.listFor(DEMO_USER_ID));
  }

  @PostMapping("/{id}/read")
  public ResponseEntity<?> read(@PathVariable Long id) {
    try {
      service.markRead(id, DEMO_USER_ID);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.status(404).body(Map.of("code","NOT_FOUND","message","알림이 없습니다"));
    }
  }

  @PostMapping("/read-all")
  public Map<String, Object> readAll() {
    int n = service.markAllRead(DEMO_USER_ID);
    return Map.of("updated", n);
  }
}
