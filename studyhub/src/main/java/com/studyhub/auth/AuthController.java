package com.studyhub.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

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
    public String doRegister(@Valid @ModelAttribute("form") RegisterForm form,
                             BindingResult bindingResult,
                             Model model){
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            auth.register(form);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e){
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e){
            model.addAttribute("error", "Unexpected error occurred.");
            return "auth/register";
        }
    }
}
