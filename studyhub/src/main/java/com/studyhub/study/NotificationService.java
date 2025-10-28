package com.studyhub.study;

import com.studyhub.domain.entity.Notification;
import com.studyhub.domain.entity.Membership;
import com.studyhub.domain.repository.NotificationRepository;
import com.studyhub.domain.repository.MembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

  private final NotificationRepository notifRepo;
  private final MembershipRepository memberRepo;

  public NotificationService(NotificationRepository notifRepo, MembershipRepository memberRepo) {
    this.notifRepo = notifRepo;
    this.memberRepo = memberRepo;
  }

  @Transactional
  public Long createForUser(Long userId, String type, Long studyId, String title, String message) {
    Notification n = new Notification();
    n.setUserId(userId);
    n.setType(type);
    n.setStudyId(studyId);
    n.setTitle(title);
    n.setMessage(message);
    n.setCreatedAt(LocalDateTime.now());
    n.setExpiresAt(LocalDateTime.now().plusDays(15));
    notifRepo.save(n);
    return n.getId();
  }

  @Transactional
  public int notifyStudyMembers(Long studyId, String type, String title, String message) {
    List<Membership> members = memberRepo.findByStudyId(studyId);
    int cnt = 0;
    for (Membership m : members) {
      createForUser(m.getUserId(), type, studyId, title, message);
      cnt++;
    }
    return cnt;
  }

  @Transactional(readOnly = true)
  public List<Notification> listFor(Long userId) {
    return notifRepo.findTop30ByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(userId, LocalDateTime.now());
  }

  @Transactional
  public void markRead(Long id, Long userId) {
    var n = notifRepo.findByIdAndUserId(id, userId).orElseThrow();
    if (n.getReadAt() == null) n.setReadAt(LocalDateTime.now());
  }

  @Transactional
  public int markAllRead(Long userId) {
    var unread = notifRepo.findByUserIdAndReadAtIsNull(userId);
    unread.forEach(n -> n.setReadAt(LocalDateTime.now()));
    return unread.size();
  }
}
