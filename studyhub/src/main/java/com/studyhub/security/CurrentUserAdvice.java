package com.studyhub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("currentUser")
    public Map<String, Object> injectCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud){
            return Map.of("id", cud.getId(), "nickname", cud.getNickname(), "username", cud.getUsername());
        }
        return null;
    }
}
