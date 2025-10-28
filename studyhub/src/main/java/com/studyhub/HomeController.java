package com.studyhub;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 루트 → home.html 렌더
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // 서버 확인용 (선택)
    @GetMapping("/ping")
    public String ping() {
        return "redirect:/";
    }
}
