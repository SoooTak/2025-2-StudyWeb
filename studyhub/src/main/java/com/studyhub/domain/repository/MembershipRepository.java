package com.studyhub.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studyhub.domain.entity.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    // 멤버 여부
    boolean existsByStudyIdAndUserId(Long studyId, Long userId);

    // 개별 멤버십
    Optional<Membership> findByStudyIdAndUserId(Long studyId, Long userId);

    // 스터디별 멤버 수
    long countByStudyId(Long studyId);

    // 스터디별 전체 멤버 조회
    List<Membership> findByStudyId(Long studyId);

    // ✅ 사용자 기준 모든 멤버십
    List<Membership> findByUserId(Long userId);
}
