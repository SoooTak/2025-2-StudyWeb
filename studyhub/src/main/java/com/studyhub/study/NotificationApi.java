package com.studyhub.study;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.studyhub.domain.entity.Notification;
import com.studyhub.security.AuthUtils;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApi {

  private final NotificationService notificationService;

  public NotificationApi(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /** 내 알림 목록 (Map 래핑: { "items": [...] } 형태 유지) */
  @GetMapping
  public Map<String, Object> list() {
    Long userId = AuthUtils.requireUserId();
    List<Notification> items = notificationService.listMy(userId);
    return Map.of("items", items);
  }

  /** 개별 읽음 처리 (204 No Content) */
  @PostMapping("/{id}/read")
  public ResponseEntity<?> read(@PathVariable Long id) {
    Long userId = AuthUtils.requireUserId();
    notificationService.markRead(userId, id);
    return ResponseEntity.noContent().build();
  }

  /** 모두 읽음 처리 (204 No Content) */
  @PostMapping("/read-all")
  public ResponseEntity<?> readAll() {
    Long userId = AuthUtils.requireUserId();
    notificationService.markAllRead(userId);
    return ResponseEntity.noContent().build();
  }
}
