package com.studyhub.study;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyhub.domain.entity.Membership;
import com.studyhub.domain.entity.Notification;
import com.studyhub.domain.repository.MembershipRepository;
import com.studyhub.domain.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final MembershipRepository memberships;

    public NotificationService(NotificationRepository notifications,
                               MembershipRepository memberships) {
        this.notifications = notifications;
        this.memberships = memberships;
    }

    /** 내 알림 목록 (최신순) */
    @Transactional(readOnly = true)
    public List<Notification> listMy(Long userId) {
        return notifications.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** 개별 읽음 처리(내 소유만) — readAt 에 시간 기록 */
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification n = notifications.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new IllegalArgumentException("알림이 없거나 권한이 없습니다"));
        if (n.getReadAt() != null) return; // 이미 읽음
        n.setReadAt(LocalDateTime.now());
        notifications.save(n);
    }

    /** 모두 읽음 처리(내 소유만) */
    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> list = notifications.findByUserIdOrderByCreatedAtDesc(userId);
        boolean changed = false;
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : list) {
            if (n.getReadAt() == null) {
                n.setReadAt(now);
                changed = true;
            }
        }
        if (changed) notifications.saveAll(list);
    }

    /**
     * 편의: 사용자에게 알림 생성
     * - Notification 엔티티는 createdAt/expiresAt 기본값을 자체 보유
     * - readAt 은 null => 미읽음
     */
    @Transactional
    public Notification createForUser(Long userId, String type, Long studyId, String title, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);         // 예: APPLIED, APPLY_PENDING, APPROVED, REJECTED, SCHEDULE_CREATED ...
        n.setStudyId(studyId);   // 컨텍스트(선택)
        n.setTitle(title);       // 간단 제목
        n.setMessage(message);   // 상세 메시지
        return notifications.save(n);
    }

    /** 배지 등에서 쓸 수 있는 미읽음 카운트 */
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notifications.countByUserIdAndReadAtIsNull(userId);
    }

    /**
     * ✅ 스터디 멤버 전체에게 동일 알림 발송
     * - 멤버십은 어떤 역할이든 모두 대상(리더/공동리더/멤버)
     * - 반환값: 생성된 알림 개수
     */
    @Transactional
    public int notifyStudyMembers(Long studyId, String type, String title, String message) {
        List<Membership> members = memberships.findByStudyId(studyId);
        int cnt = 0;
        for (Membership m : members) {
            Long uid = m.getUserId();
            if (uid == null) continue;
            createForUser(uid, type, studyId, title, message);
            cnt++;
        }
        return cnt;
    }
}
