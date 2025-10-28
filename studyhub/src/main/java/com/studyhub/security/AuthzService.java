package com.studyhub.security;

import org.springframework.stereotype.Service;

import com.studyhub.domain.entity.Membership;
import com.studyhub.domain.entity.Application;
import com.studyhub.domain.repository.MembershipRepository;
import com.studyhub.domain.repository.ApplicationRepository;

@Service("authz")
public class AuthzService {

    private final MembershipRepository memberships;
    private final ApplicationRepository applications;

    public AuthzService(MembershipRepository memberships,
                        ApplicationRepository applications) {
        this.memberships = memberships;
        this.applications = applications;
    }

    /** 현재 로그인 사용자가 해당 스터디의 관리자(리더/공동리더)인가? (studyId 기준) */
    public boolean canManageStudy(Long studyId) {
        Long userId = AuthUtils.currentUserIdOrNull();
        if (userId == null) return false;

        Membership m = memberships.findByStudyIdAndUserId(studyId, userId).orElse(null);
        if (m == null) return false;

        Object roleObj = m.getRole(); // String/Enum 모두 허용
        if (roleObj == null) return false;

        String role = roleObj.toString();
        return "LEADER".equalsIgnoreCase(role) || "COLEADER".equalsIgnoreCase(role);
    }

    /** 현재 로그인 사용자가 해당 "신청(applicationId)"이 속한 스터디의 관리자(리더/공동리더)인가? */
    public boolean canManageApplication(Long applicationId) {
        Long userId = AuthUtils.currentUserIdOrNull();
        if (userId == null) return false;

        Application app = applications.findById(applicationId).orElse(null);
        if (app == null) return false;

        Long studyId = app.getStudyId();
        if (studyId == null) return false;

        Membership m = memberships.findByStudyIdAndUserId(studyId, userId).orElse(null);
        if (m == null) return false;

        Object roleObj = m.getRole();
        if (roleObj == null) return false;

        String role = roleObj.toString();
        return "LEADER".equalsIgnoreCase(role) || "COLEADER".equalsIgnoreCase(role);
    }

    /** 현재 로그인 사용자가 해당 스터디의 멤버인가? */
    public boolean isMemberOf(Long studyId) {
        Long userId = AuthUtils.currentUserIdOrNull();
        if (userId == null) return false;
        return memberships.existsByStudyIdAndUserId(studyId, userId);
    }
}
