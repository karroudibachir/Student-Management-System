package com.sms.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AppUserService users;
    public AuthController(AppUserService users) { this.users = users; }
    @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return users.login(request);
    }
    @GetMapping("/me") public AppUserView me(@AuthenticationPrincipal Jwt jwt) {
        return users.currentUser(jwt.getSubject());
    }
}
