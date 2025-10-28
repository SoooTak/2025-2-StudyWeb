package com.studyhub.auth;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterForm {
    private String username; // 소문자만 허용(서버에서 toLowerCase)
    private String password;
    private String nickname;
    private String name;
    private String email;
    private String phone;
}
