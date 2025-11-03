package com.studyhub.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studyhub.domain.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByIdAndStatus(Long id, String status);

    // 스터디+사용자 기준 대기중 중복 검사
    Optional<Application> findByStudyIdAndUserIdAndStatus(Long studyId, Long userId, String status);

    // 본인 신청 취소용(대기중만)
    Optional<Application> findByIdAndUserIdAndStatus(Long id, Long userId, String status);

    // 스터디별 특정 상태 전체
    List<Application> findByStudyIdAndStatus(Long studyId, String status);

    // 특정 상태를 생성일 오름차순으로
    List<Application> findByStudyIdAndStatusOrderByCreatedAtAsc(Long studyId, String status);

    // ✅ 사용자 기준 대기중 신청 목록
    List<Application> findByUserIdAndStatus(Long userId, String status);
}
