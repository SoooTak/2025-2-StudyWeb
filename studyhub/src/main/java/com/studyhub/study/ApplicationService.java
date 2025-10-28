package com.studyhub.study;

import com.studyhub.domain.entity.Application;
import com.studyhub.domain.entity.Membership;
import com.studyhub.domain.entity.Study;
import com.studyhub.domain.repository.ApplicationRepository;
import com.studyhub.domain.repository.MembershipRepository;
import com.studyhub.domain.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

  private final ApplicationRepository appRepo;
  private final MembershipRepository memberRepo;
  private final StudyRepository studyRepo;
  private final NotificationService notificationService;

  public ApplicationService(ApplicationRepository appRepo,
                            MembershipRepository memberRepo,
                            StudyRepository studyRepo,
                            NotificationService notificationService) {
    this.appRepo = appRepo;
    this.memberRepo = memberRepo;
    this.studyRepo = studyRepo;
    this.notificationService = notificationService;
  }

  @Transactional
  public String approve(Long appId, Long actorId) {
    Application app = appRepo.findByIdAndStatus(appId, "PENDING")
        .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없거나 대기중이 아닙니다"));

    Study study = studyRepo.findWithLockingById(app.getStudyId())
        .orElseThrow(() -> new IllegalArgumentException("스터디가 없습니다"));
    if (!"OPEN".equalsIgnoreCase(study.getStatus())) {
      throw new IllegalStateException("모집 마감된 스터디입니다");
    }

    long current = memberRepo.countByStudyId(study.getId());
    if (current >= study.getCapacity()) {
      throw new IllegalStateException("정원이 가득 찼습니다");
    }

    if (!memberRepo.existsByStudyIdAndUserId(study.getId(), app.getUserId())) {
      Membership m = new Membership();
      m.setStudyId(study.getId());
      m.setUserId(app.getUserId());
      m.setRole("MEMBER");
      memberRepo.save(m);
    }

    app.setStatus("APPROVED");
    appRepo.save(app);

    // 알림: 승인 (신청자에게)
    notificationService.createForUser(
        app.getUserId(),
        "APPROVED",
        study.getId(),
        "가입 승인",
        "‘" + study.getTitle() + "’ 가입이 승인되었습니다."
    );
    return "APPROVED";
  }

  @Transactional
  public String reject(Long appId, Long actorId) {
    Application app = appRepo.findByIdAndStatus(appId, "PENDING")
        .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없거나 대기중이 아닙니다"));
    app.setStatus("REJECTED");
    appRepo.save(app);

    // 알림: 거절 (신청자에게)
    notificationService.createForUser(
        app.getUserId(),
        "REJECTED",
        app.getStudyId(),
        "가입 거절",
        "신청하신 스터디 가입이 거절되었습니다."
    );
    return "REJECTED";
  }
}
