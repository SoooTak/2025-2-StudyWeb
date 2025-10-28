package com.studyhub.domain.repository;

import com.studyhub.domain.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
  List<StudySession> findByStudyIdOrderByStartAtAsc(Long studyId);
}
