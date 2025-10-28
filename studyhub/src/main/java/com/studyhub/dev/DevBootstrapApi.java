package com.studyhub.dev;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.studyhub.security.AuthUtils;
import com.studyhub.domain.entity.Membership;
import com.studyhub.domain.entity.Study;
import com.studyhub.domain.repository.MembershipRepository;
import com.studyhub.domain.repository.StudyRepository;

@RestController
@RequestMapping("/api/dev")
public class DevBootstrapApi {

  private final StudyRepository studyRepo;
  private final MembershipRepository memberRepo;

  public DevBootstrapApi(StudyRepository studyRepo, MembershipRepository memberRepo) {
    this.studyRepo = studyRepo;
    this.memberRepo = memberRepo;
  }

  /**
   * 로그인한 사용자를 리더로 지정한 "테스트 스터디"를 즉시 생성.
   *
   * 예) /api/dev/make-leader-study
   *     /api/dev/make-leader-study?title=알고리즘&category=CS&capacity=12
   */
  @GetMapping("/make-leader-study")
  public ResponseEntity<Map<String, Object>> makeLeaderStudy(
      @RequestParam(defaultValue = "테스트 스터디") String title,
      @RequestParam(defaultValue = "CS") String category,
      @RequestParam(defaultValue = "10") int capacity
  ) {
    Long userId = AuthUtils.requireUserId(); // 로그인 필수

    // 1) 스터디 생성
    Study s = new Study();
    s.setTitle(title);
    s.setDescription("개발용 자동 생성 스터디");
    s.setCategory(category);
    s.setCapacity(capacity);
    s.setStatus("OPEN"); // 모집중
    studyRepo.save(s);

    // 2) 현재 사용자 리더로 등록(업서트)
    Membership m = memberRepo.findByStudyIdAndUserId(s.getId(), userId).orElseGet(Membership::new);
    m.setStudyId(s.getId());
    m.setUserId(userId);
    m.setRole("LEADER");
    memberRepo.save(m);

    Map<String, Object> body = new HashMap<>();
    body.put("message", "생성 완료");
    body.put("studyId", s.getId());
    body.put("studyTitle", s.getTitle());
    body.put("leaderUserId", userId);
    return ResponseEntity.ok(body);
  }

  /**
   * ✅ 기존 스터디에서 "현재 로그인한 나"를 리더/공동리더/멤버로 승격/등록(업서트)
   * - 개발 편의를 위한 임시 API (로그인만 되어 있으면 호출 가능)
   *
   * 예)
   *  - /api/dev/promote-self?studyId=4            (기본: role=LEADER)
   *  - /api/dev/promote-self?studyId=4&role=COLEADER
   *  - /api/dev/promote-self?studyId=4&role=MEMBER
   */
  @GetMapping("/promote-self")
  public ResponseEntity<Map<String, Object>> promoteSelf(
      @RequestParam Long studyId,
      @RequestParam(defaultValue = "LEADER") String role
  ) {
    Long userId = AuthUtils.requireUserId();

    // 스터디 존재 체크(없으면 404)
    Study s = studyRepo.findById(studyId)
        .orElseThrow(() -> new IllegalArgumentException("스터디가 없습니다: id=" + studyId));

    // 업서트
    Membership m = memberRepo.findByStudyIdAndUserId(studyId, userId).orElseGet(Membership::new);
    m.setStudyId(studyId);
    m.setUserId(userId);
    m.setRole(role); // "LEADER" | "COLEADER" | "MEMBER"
    memberRepo.save(m);

    Map<String, Object> body = new HashMap<>();
    body.put("message", "OK");
    body.put("studyId", studyId);
    body.put("role", role);
    body.put("userId", userId);
    return ResponseEntity.ok(body);
  }
}
