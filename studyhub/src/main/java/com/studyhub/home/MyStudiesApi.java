package com.studyhub.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired; // Optional injection

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * 내 스터디 묶음 조회 파사드 API
 *
 * - domain/service/repository 에 직접 의존하지 않는 얇은 파사드.
 * - 실데이터 연동 시 MyStudiesReadService 의 @Service 구현을 추가하면 자동 사용.
 * - 만약 구현 빈이 없으면, 컨트롤러 내부에서 안전 기본 구현(Empty)을 사용합니다.
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

    /**
     * 주입 가능한 빈이 없으면 자동으로 Empty 구현을 사용하도록 처리합니다.
     * - @Autowired(required = false) 로 선택 주입
     * - null 이면 new EmptyMyStudiesReadService() 로 대체
     */
    public MyStudiesApi(@Autowired(required = false) MyStudiesReadService readService) {
        if (readService == null) {
            log.info("[MyStudies] No MyStudiesReadService bean found. Using Empty fallback.");
            this.readService = new EmptyMyStudiesReadService();
        } else {
            this.readService = readService;
        }
    }

    @GetMapping
    public MyStudiesResponse getMyStudies(Authentication authentication) {
        MyStudiesResponse resp = readService.readForCurrentUser(authentication);
        if (resp == null) {
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
     * 실제 구현체는 @Service 로 등록하여 주입하세요.
     * 예) DB 조회로 현재 사용자 기준 leader/member/pending 목록 구성.
     */
    public interface MyStudiesReadService {
        MyStudiesResponse readForCurrentUser(Authentication auth);
    }

    /**
     * 안전 기본 구현(빈 목록 반환). 스프링 빈이 아니며,
     * 컨트롤러 생성자에서 주입 실패 시 직접 생성하여 사용합니다.
     */
    public static class EmptyMyStudiesReadService implements MyStudiesReadService {
        @Override
        public MyStudiesResponse readForCurrentUser(Authentication auth) {
            String who = (auth != null ? String.valueOf(auth.getName()) : "anonymous");
            log.info("[MyStudies] EmptyMyStudiesReadService used. principal={}", who);
            return MyStudiesResponse.empty();
        }
    }
}
