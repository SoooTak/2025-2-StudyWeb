package com.studyhub.study;

import com.studyhub.domain.entity.Study;
import com.studyhub.domain.repository.StudyRepository;
import com.studyhub.domain.repository.MembershipRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/studies")
public class StudyApi {

  private final StudyRepository repo;
  private final MembershipRepository memberRepo;

  public StudyApi(StudyRepository repo, MembershipRepository memberRepo) {
    this.repo = repo;
    this.memberRepo = memberRepo;
  }

  @GetMapping
  public List<StudyListItem> list(
      @RequestParam(required = false) String category,
      @RequestParam(defaultValue = "latest") String sort // latest | deadline
  ) {
    List<Study> rows = (category == null || category.isBlank())
        ? repo.findAll()
        : repo.findByCategoryIgnoreCase(category);

    if ("deadline".equalsIgnoreCase(sort)) {
      rows = rows.stream()
          .sorted(java.util.Comparator.comparing(
              Study::getApplyDeadline,
              java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
          ))
          .toList();
    } else { // latest (createdAt DESC)
      rows = rows.stream()
          .sorted(java.util.Comparator.comparing(Study::getCreatedAt).reversed())
          .toList();
    }

    return rows.stream().map(this::toListItem).toList();
  }

  @GetMapping("/{id}")
  public StudyDetail detail(@PathVariable Long id) {
    var s = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("스터디가 없습니다"));
    long current = memberRepo.countByStudyId(s.getId());
    var display = computeDisplayStatus(s, current);
    return new StudyDetail(
        s.getId(), s.getTitle(), s.getDescription(), s.getCategory(),
        s.getCapacity(), s.getStatus(), s.getImageUrl(),
        s.getApplyDeadline(), current, display
    );
  }

  // ====== DTO & helper ======
  private StudyListItem toListItem(Study s) {
    long current = memberRepo.countByStudyId(s.getId());
    String display = computeDisplayStatus(s, current);
    return new StudyListItem(
        s.getId(), s.getTitle(), s.getCategory(), s.getStatus(), s.getImageUrl(),
        s.getApplyDeadline(), current, s.getCapacity(), display
    );
  }

  // 전시용 상태 결정:
  // status != OPEN → "마감"
  // applyDeadline 지남 → "마감"
  // current >= capacity → "마감"
  // 그 외 → "모집중"
  private String computeDisplayStatus(Study s, long current){
    if (!"OPEN".equalsIgnoreCase(s.getStatus())) return "마감";
    if (s.getApplyDeadline() != null && LocalDateTime.now().isAfter(s.getApplyDeadline())) return "마감";
    if (s.getCapacity() != null && current >= s.getCapacity()) return "마감";
    return "모집중";
  }

  public record StudyListItem(
      Long id, String title, String category, String status, String imageUrl,
      java.time.LocalDateTime applyDeadline, long current, Integer capacity, String displayStatus
  ) {}

  public record StudyDetail(
      Long id, String title, String description, String category,
      Integer capacity, String status, String imageUrl,
      java.time.LocalDateTime applyDeadline, long current, String displayStatus
  ) {}
}
