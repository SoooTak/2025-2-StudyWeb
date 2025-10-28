package com.studyhub.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyhub.user.User;
import com.studyhub.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterForm form){
        String username = form.getUsername().toLowerCase().trim();
        if (!username.matches("^[a-z0-9_]{3,20}$")) {
            throw new IllegalArgumentException("아이디는 소문자/숫자/밑줄, 3~20자");
        }
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (users.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        String encoded = passwordEncoder.encode(form.getPassword());
        User u = User.builder()
                .username(username)
                .passwordHash(encoded)
                .nickname(form.getNickname())
                .name(form.getName())
                .email(form.getEmail())
                .phone(form.getPhone())
                .build();
        return users.save(u);
    }
}
