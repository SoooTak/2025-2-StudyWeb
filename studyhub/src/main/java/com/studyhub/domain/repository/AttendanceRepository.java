package com.studyhub.domain.repository;

import com.studyhub.domain.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
  Optional<Attendance> findBySessionIdAndUserId(Long sessionId, Long userId);
}
