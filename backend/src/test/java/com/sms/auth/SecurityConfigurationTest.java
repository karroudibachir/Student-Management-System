package com.sms.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest {
    @Mock AppUserRepository users;

    @Test void mapsDatabaseRolesToSpringAuthorities() {
        AppUser admin = new AppUser("admin-sub", "admin@example.edu", "Admin");
        admin.getRoles().add(AppRole.ADMIN);
        when(users.findById("admin-sub")).thenReturn(Optional.of(admin));
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("admin-sub")
                .claim("scope", "openid profile").issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(120)).build();
        var authentication = new SecurityConfiguration().jwtAuthenticationConverter(users).convert(jwt);
        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_openid")));
    }
}
