package com.studyhub.security;

import org.springframework.stereotype.Service;

import com.studyhub.membership.MembershipRepository;
import com.studyhub.membership.MembershipRole;

@Service("authz")
public class AuthzService {

    private final MembershipRepository memberships;

    public AuthzService(MembershipRepository memberships) {
        this.memberships = memberships;
    }

    /**
     * 현재 로그인 사용자가 해당 스터디의 관리자(리더/공동리더)인가?
     */
    public boolean canManageStudy(Long studyId) {
        Long userId = AuthUtils.currentUserIdOrNull();
        if (userId == null) return false;
        var m = memberships.findByStudyIdAndUserId(studyId, userId).orElse(null);
        if (m == null) return false;
        var r = m.getRole();
        return r == MembershipRole.LEADER || r == MembershipRole.COLEADER;
    }

    /**
     * 현재 로그인 사용자가 해당 스터디의 멤버인가?
     */
    public boolean isMemberOf(Long studyId) {
        Long userId = AuthUtils.currentUserIdOrNull();
        if (userId == null) return false;
        return memberships.existsByStudyIdAndUserId(studyId, userId);
    }
}
