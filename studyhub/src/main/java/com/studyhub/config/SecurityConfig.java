package com.studyhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

import com.studyhub.security.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider authProvider) throws Exception {
        // SavedRequest: 브라우저 네비게이션만 저장( /api/** 및 AJAX는 저장 안 함 )
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(
            new AndRequestMatcher(
                new AntPathRequestMatcher("/**"),
                new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**"))
            )
        );

        http
            .authorizeHttpRequests(auth -> auth
                // 완전 공개(정적 리소스 포함)
                .requestMatchers("/", "/error", "/favicon.ico",
                        "/css/**", "/js/**", "/images/**",
                        "/app.css", "/notify.js", "/home.js").permitAll()
                // 공개 화면
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/explore", "/explore/**").permitAll()
                // 공개 API
                .requestMatchers("/api/studies/**").permitAll()
                // 인증 필요
                .requestMatchers("/notifications/**", "/api/notifications/**").authenticated()
                .requestMatchers("/sessions/**", "/api/sessions/**").authenticated()
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")   // POST /login
                .defaultSuccessUrl("/", false)
                .failureUrl("/login?error")
                .permitAll()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .exceptionHandling(e -> e.authenticationEntryPoint(
                new LoginUrlAuthenticationEntryPoint("/login")
            ))
            .requestCache(c -> c.requestCache(requestCache))   // ✔ SavedRequest 커스터마이즈
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .authenticationProvider(authProvider);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(CustomUserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
