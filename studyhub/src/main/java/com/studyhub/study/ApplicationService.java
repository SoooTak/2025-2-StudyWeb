package com.studyhub.study;

import com.studyhub.domain.entity.Application;
import com.studyhub.domain.entity.Membership;
import com.studyhub.domain.entity.Study;
import com.studyhub.domain.repository.ApplicationRepository;
import com.studyhub.domain.repository.MembershipRepository;
import com.studyhub.domain.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

  /**
   * 스터디 지원 (로그인 사용자)
   * - 모집 상태 OPEN 확인
   * - 이미 멤버인지 확인
   * - 대기중(PENDING) 신청 중복 방지
   */
  @Transactional
  public String apply(Long studyId, Long userId) {
    Study study = studyRepo.findById(studyId)
        .orElseThrow(() -> new IllegalArgumentException("스터디가 없습니다"));

    if (!"OPEN".equalsIgnoreCase(study.getStatus())) {
      throw new IllegalStateException("모집 마감된 스터디입니다");
    }

    // 이미 멤버인지
    if (memberRepo.existsByStudyIdAndUserId(studyId, userId)) {
      throw new IllegalStateException("이미 멤버입니다");
    }

    // 이미 '대기중' 신청이 있는지
    var dup = appRepo.findByStudyIdAndUserIdAndStatus(studyId, userId, "PENDING");
    if (dup.isPresent()) {
      throw new IllegalStateException("이미 대기중인 신청이 있습니다");
    }

    // 신청 생성
    Application app = new Application();
    app.setStudyId(studyId);
    app.setUserId(userId);
    app.setStatus("PENDING");
    appRepo.save(app);

    // 알림: 신청자 본인에게 접수 안내
    notificationService.createForUser(
        userId,
        "APPLIED",
        studyId,
        "가입 신청 접수",
        "‘" + study.getTitle() + "’ 가입 신청이 접수되었습니다."
    );

    // 알림: 리더/공동리더에게 대기중 신청 알림
    List<Membership> managers = memberRepo.findByStudyId(studyId);
    for (Membership m : managers) {
      Object roleObj = m.getRole();
      String role = roleObj == null ? "" : roleObj.toString();
      if ("LEADER".equalsIgnoreCase(role) || "COLEADER".equalsIgnoreCase(role)) {
        notificationService.createForUser(
            m.getUserId(),
            "APPLY_PENDING",
            studyId,
            "새 가입 신청",
            "스터디 ‘" + study.getTitle() + "’에 새 가입 신청이 있습니다."
        );
      }
    }

    return "PENDING";
  }

  /**
   * 내 신청 취소 (대기중만)
   */
  @Transactional
  public void cancel(Long applicationId, Long userId) {
    Application app = appRepo.findByIdAndUserIdAndStatus(applicationId, userId, "PENDING")
        .orElseThrow(() -> new IllegalArgumentException("대기중 신청이 없거나 권한이 없습니다"));

    app.setStatus("CANCELED");
    appRepo.save(app);

    // 알림: 본인에게 취소 안내
    notificationService.createForUser(
        userId,
        "CANCELED",
        app.getStudyId(),
        "가입 신청 취소",
        "가입 신청이 취소되었습니다."
    );
  }

  @Transactional
  public String approve(Long appId, Long actorId) {
    Application app = appRepo.findByIdAndStatus(appId, "PENDING")
        .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없거나 대기중이 아닙니다"));

    Study study = studyRepo.findById(app.getStudyId())
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
