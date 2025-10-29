package com.studyhub.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * 내 스터디 묶음 조회 파사드 API
 *
 * - 경로/시그니처 충돌 방지를 위해 domain/service/repository 에 의존하지 않는 얇은 파사드.
 * - 실제 데이터 연동은 MyStudiesReadService 빈을 별도 구현하여 주입하면 자동 전환됨.
 * - 기본 구현(default bean)은 빈 리스트를 반환하므로 기존 기능에 영향 없음.
 *
 * GET /api/mystudies
 * 응답:
 * {
 *   "leader":  [StudyCard...],
 *   "member":  [StudyCard...],
 *   "pending": [StudyCard...]
 * }
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/mystudies", produces = MediaType.APPLICATION_JSON_VALUE)
public class MyStudiesApi {

    private final MyStudiesReadService readService;

    public MyStudiesApi(MyStudiesReadService readService) {
        this.readService = readService;
    }

    @GetMapping
    public MyStudiesResponse getMyStudies(Authentication authentication) {
        // 인증은 SecurityConfig에서 이 경로를 보호하고 있다고 가정 (로그인 필요)
        // 혹시 공개로 열려 있어도, 기본 구현은 빈 목록만 반환하므로 오류 없이 동작.
        MyStudiesResponse resp = readService.readForCurrentUser(authentication);
        if (resp == null) {
            // NPE 방지: 방어적으로 빈 응답
            resp = MyStudiesResponse.empty();
        }
        return resp;
    }

    /* ===================== DTO ===================== */

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MyStudiesResponse {
        private List<StudyCard> leader;
        private List<StudyCard> member;
        private List<StudyCard> pending;

        public static MyStudiesResponse empty() {
            return MyStudiesResponse.builder()
                    .leader(Collections.emptyList())
                    .member(Collections.emptyList())
                    .pending(Collections.emptyList())
                    .build();
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StudyCard {
        private Long id;
        private String title;
        private String category;       // 예: "알고리즘", "면접", ...
        private String status;         // 예: "OPEN" / "CLOSED"
        private Integer capacity;      // 정원
        private Integer memberCount;   // 현재 멤버 수
        private String imageUrl;       // 썸네일(없으면 null/빈문자)
        private Instant applyDeadline; // 선택: 지원 마감 (없으면 null)
    }

    /* ============ 포트(인터페이스) & 안전 기본 구현 ============ */

    /**
     * 실제 구현체는 별도 파일에서 @Service 로 등록하여 주입하세요.
     * 예) DB 조회로 현재 사용자 기준 leader/member/pending 목록 구성.
     */
    public interface MyStudiesReadService {
        MyStudiesResponse readForCurrentUser(Authentication auth);
    }

    /**
     * 안전 기본 구현: 빈 목록 반환
     * - 프로젝트에 별도 구현체가 없을 때 자동 등록
     * - 실데이터 구현(@Service)이 존재하면 이 빈은 등록되지 않음
     */
    @Component
    @ConditionalOnMissingBean(MyStudiesReadService.class)
    public static class EmptyMyStudiesReadService implements MyStudiesReadService {
        @Override
        public MyStudiesResponse readForCurrentUser(Authentication auth) {
            // 로그로 호출 주체를 남겨 디버깅 편의 제공
            String who = (auth != null ? String.valueOf(auth.getName()) : "anonymous");
            log.info("[MyStudies] fallback EmptyMyStudiesReadService used. principal={}", who);
            return MyStudiesResponse.empty();
        }
    }
}
