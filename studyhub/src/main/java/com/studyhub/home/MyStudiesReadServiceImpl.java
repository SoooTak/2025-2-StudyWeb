package com.studyhub.home;

import com.studyhub.domain.entity.Application;
import com.studyhub.domain.entity.Membership;
import com.studyhub.domain.entity.Study;
import com.studyhub.domain.repository.ApplicationRepository;
import com.studyhub.domain.repository.MembershipRepository;
import com.studyhub.domain.repository.StudyRepository;
import com.studyhub.home.MyStudiesApi.MyStudiesReadService;
import com.studyhub.home.MyStudiesApi.MyStudiesResponse;
import com.studyhub.home.MyStudiesApi.StudyCard;
import com.studyhub.user.User;
import com.studyhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * /api/mystudies 실데이터 구현
 * - leader  : role IN (LEADER, CO_LEADER)
 * - member  : role = MEMBER
 * - pending : application.status = PENDING
 *
 * ✅ CustomUserDetails 내부 구현(getUser(), getId() 등)에 의존하지 않고
 *    Authentication.getName()으로 username을 얻은 뒤 UserRepository로 userId를 조회합니다.
 */
@Service
@RequiredArgsConstructor
public class MyStudiesReadServiceImpl implements MyStudiesReadService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final ApplicationRepository applicationRepository;
    private final StudyRepository studyRepository;

    @Override
    public MyStudiesResponse readForCurrentUser(Authentication auth) {
        Long userId = resolveUserId(auth);
        if (userId == null) {
            // 미로그인 혹은 사용자 조회 실패시: 빈 목록 반환
            return MyStudiesResponse.empty();
        }

        // 1) 내 모든 멤버십
        List<Membership> memberships = membershipRepository.findByUserId(userId);

        // 2) 내가 낸 대기중 신청
        List<Application> pendings = applicationRepository.findByUserIdAndStatus(userId, "PENDING");

        List<StudyCard> leader = new ArrayList<>();
        List<StudyCard> member = new ArrayList<>();
        List<StudyCard> pending = new ArrayList<>();

        ZoneId zone = ZoneId.systemDefault();

        // 멤버십 → 카드화
        for (Membership m : memberships) {
            Optional<Study> opt = studyRepository.findById(m.getStudyId());
            if (opt.isEmpty()) continue;
            Study s = opt.get();

            StudyCard card = StudyCard.builder()
                    .id(s.getId())
                    .title(s.getTitle())
                    .category(s.getCategory())
                    .status(s.getStatus())
                    .capacity(s.getCapacity())
                    .memberCount((int) membershipRepository.countByStudyId(s.getId()))
                    .imageUrl(s.getImageUrl())
                    .applyDeadline(s.getApplyDeadline() == null ? null : s.getApplyDeadline().atZone(zone).toInstant())
                    .build();

            String role = m.getRole() == null ? "" : m.getRole();
            if ("LEADER".equalsIgnoreCase(role) || "CO_LEADER".equalsIgnoreCase(role)) {
                leader.add(card);
            } else {
                member.add(card);
            }
        }

        // 대기중 신청 → 카드화
        for (Application a : pendings) {
            Optional<Study> opt = studyRepository.findById(a.getStudyId());
            if (opt.isEmpty()) continue;
            Study s = opt.get();

            StudyCard card = StudyCard.builder()
                    .id(s.getId())
                    .title(s.getTitle())
                    .category(s.getCategory())
                    .status(s.getStatus())
                    .capacity(s.getCapacity())
                    .memberCount((int) membershipRepository.countByStudyId(s.getId()))
                    .imageUrl(s.getImageUrl())
                    .applyDeadline(s.getApplyDeadline() == null ? null : s.getApplyDeadline().atZone(zone).toInstant())
                    .build();

            pending.add(card);
        }

        // 최신순 정렬 (createdAt 기준이 가장 자연스럽지만 null-safe를 위해 id 역순도 보조로 사용)
        Comparator<StudyCard> byIdDesc = Comparator.comparingLong(StudyCard::getId).reversed();
        leader.sort(byIdDesc);
        member.sort(byIdDesc);
        pending.sort(byIdDesc);

        return MyStudiesResponse.builder()
                .leader(leader)
                .member(member)
                .pending(pending)
                .build();
    }

    /**
     * Authentication → username → UserRepository → userId
     */
    private Long resolveUserId(Authentication auth) {
        if (auth == null) return null;
        String username = auth.getName();
        if (username == null || username.isBlank()) return null;
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.map(User::getId).orElse(null);
    }
}
