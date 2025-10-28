package com.studyhub.domain.repository;

import com.studyhub.domain.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
  long countByStudyId(Long studyId);
  boolean existsByStudyIdAndUserId(Long studyId, Long userId);
  List<Membership> findByStudyId(Long studyId); // 알림 전파용
}
