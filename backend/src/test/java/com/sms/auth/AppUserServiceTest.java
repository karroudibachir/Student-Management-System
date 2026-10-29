package com.sms.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {
    @Mock AppUserRepository users;

    @Test void firstLoginGetsViewerUnlessBootstrapEmailMatches() {
        AppUserService service = new AppUserService(users, "admin@example.edu");
        when(users.findById("admin-sub")).thenReturn(Optional.empty());
        when(users.save(any(AppUser.class))).thenAnswer(call -> call.getArgument(0));
        AppUserView view = service.currentUser(jwt("admin-sub", "admin@example.edu"));
        assertEquals(Set.of(AppRole.ADMIN), view.roles());
    }

    @Test void ordinaryFirstLoginGetsReadOnlyRole() {
        AppUserService service = new AppUserService(users, "admin@example.edu");
        when(users.findById("viewer-sub")).thenReturn(Optional.empty());
        when(users.save(any(AppUser.class))).thenAnswer(call -> call.getArgument(0));
        AppUserView view = service.currentUser(jwt("viewer-sub", "reader@example.edu"));
        assertEquals(Set.of(AppRole.VIEWER), view.roles());
    }

    @Test void refusesToRemoveTheFinalAdministrator() {
        AppUserService service = new AppUserService(users, "");
        AppUser admin = new AppUser("admin-sub", "admin@example.edu", "Admin");
        admin.getRoles().add(AppRole.ADMIN);
        when(users.findById("admin-sub")).thenReturn(Optional.of(admin));
        when(users.countUsersWithRole(AppRole.ADMIN)).thenReturn(1L);
        assertThrows(LastAdministratorException.class,
                () -> service.updateRoles("admin-sub", Set.of(AppRole.VIEWER)));
        verify(users, never()).save(any());
    }

    private Jwt jwt(String subject, String email) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject)
                .claim("email", email)
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
