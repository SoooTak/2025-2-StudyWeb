package com.studyhub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {

    public static Long currentUserIdOrNull(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) return null;
        return cud.getId();
    }

    public static Long requireUserId(){
        Long id = currentUserIdOrNull();
        if (id == null) throw new RuntimeException("로그인이 필요합니다.");
        return id;
    }

    public static String currentNicknameOrNull(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) return null;
        return cud.getNickname();
    }
}
