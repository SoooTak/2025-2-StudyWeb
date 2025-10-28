package com.studyhub.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute("form") RegisterForm form, Model model){
        try {
            auth.register(form);
            // 회원가입 성공 → 로그인 페이지로
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e){
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e){
            model.addAttribute("error", "알 수 없는 오류가 발생했습니다.");
            return "auth/register";
        }
    }
}
