package com.studyhub.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studyhub.domain.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 특정 사용자 알림 목록(최신순) */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** 소유자 검증용 단건 조회 */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    /** 읽지 않은 알림 수 (배지용) — read_at 이 NULL인 건 미읽음 */
    long countByUserIdAndReadAtIsNull(Long userId);
}
