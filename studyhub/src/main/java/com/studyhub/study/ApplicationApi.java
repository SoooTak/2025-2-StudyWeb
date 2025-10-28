package com.studyhub.study;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.studyhub.security.AuthUtils;

@RestController
@RequestMapping("/api")
public class ApplicationApi {

    private final ApplicationService applicationService;

    public ApplicationApi(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** 스터디 지원(로그인 필요) — 기존 서비스 시그니처를 유지한다고 가정 */
    @PostMapping("/studies/{studyId}/apply")
    public ResponseEntity<?> apply(@PathVariable Long studyId) {
        Long userId = AuthUtils.requireUserId(); // 현재 로그인 사용자
        return ResponseEntity.ok(applicationService.apply(studyId, userId));
    }

    /** 내 지원 취소(로그인 필요) */
    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<?> cancel(@PathVariable Long applicationId) {
        Long userId = AuthUtils.requireUserId();
        applicationService.cancel(applicationId, userId);
        return ResponseEntity.noContent().build();
    }

    /** (리더/공동리더) 지원자 승인 — applicationId로 권한 판별 */
    @PreAuthorize("@authz.canManageApplication(#applicationId)")
    @PostMapping("/applications/{applicationId}/approve")
    public ResponseEntity<?> approve(@PathVariable Long applicationId) {
        Long actorId = AuthUtils.requireUserId();
        return ResponseEntity.ok(applicationService.approve(applicationId, actorId));
    }

    /** (리더/공동리더) 지원자 거절 — applicationId로 권한 판별 */
    @PreAuthorize("@authz.canManageApplication(#applicationId)")
    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<?> reject(@PathVariable Long applicationId) {
        Long actorId = AuthUtils.requireUserId();
        return ResponseEntity.ok(applicationService.reject(applicationId, actorId));
    }
}
