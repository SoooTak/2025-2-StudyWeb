package com.studyhub.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterForm {
    @NotBlank(message = "아이디를 입력하세요.")
    private String username; // 영문자/숫자/밑줄, 서버에서 소문자로 처리

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;

    @NotBlank(message = "닉네임을 입력하세요.")
    private String nickname;

    @NotBlank(message = "이름을 입력하세요.")
    private String name;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private String phone;
}

