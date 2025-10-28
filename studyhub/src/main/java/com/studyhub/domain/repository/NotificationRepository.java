package com.studyhub.domain.repository;

import com.studyhub.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findTop30ByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime now);
  Optional<Notification> findByIdAndUserId(Long id, Long userId);
  List<Notification> findByUserIdAndReadAtIsNull(Long userId);
}
