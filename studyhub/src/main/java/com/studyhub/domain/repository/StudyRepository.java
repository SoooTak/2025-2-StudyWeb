package com.studyhub.domain.repository;

import com.studyhub.domain.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {
  List<Study> findByCategoryIgnoreCase(String category);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Study> findWithLockingById(Long id);
}