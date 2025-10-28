package com.studyhub.domain.repository;

import com.studyhub.domain.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;


public interface ApplicationRepository extends JpaRepository<Application, Long> {
  Optional<Application> findByStudyIdAndUserIdAndStatus(Long studyId, Long userId, String status);

  // 승인 시 같은 신청을 중복 승인하지 않도록 PESSIMISTIC_WRITE 잠금
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Application> findByIdAndStatus(Long id, String status);

  List<Application> findByStudyIdAndStatusOrderByCreatedAtAsc(Long studyId, String status);
}