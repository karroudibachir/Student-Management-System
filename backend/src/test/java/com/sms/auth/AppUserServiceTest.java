package com.sms.auth;

import com.sms.student.Student;
import com.sms.student.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {
    @Mock AppUserRepository users;
    @Mock StudentRepository students;
    @Mock PasswordEncoder passwords;
    @Mock JwtEncoder jwtEncoder;

    private AppUserService service(String email, String password) {
        return new AppUserService(users, students, passwords, jwtEncoder, email, password);
    }

    @Test void bootstrapCreatesAnAdministratorWithAnEncodedPassword() {
        when(users.findByEmailIgnoreCase("admin@example.edu")).thenReturn(Optional.empty());
        when(passwords.encode("a-strong-password")).thenReturn("bcrypt-hash");
        when(users.save(any())).thenAnswer(call -> call.getArgument(0));
        service("admin@example.edu", "a-strong-password").bootstrapAdmin();
        var saved = org.mockito.ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(saved.capture());
        assertEquals("bcrypt-hash", saved.getValue().getPasswordHash());
        assertTrue(saved.getValue().getRoles().contains(AppRole.ADMIN));
    }

    @Test void loginRequiresThePasswordEncoderAndReturnsASignedJwt() {
        AppUser user = new AppUser("user-sub", "student@example.edu", "Student User");
        user.setPasswordHash("stored-hash");
        when(users.findByEmailIgnoreCase("student@example.edu")).thenReturn(Optional.of(user));
        when(passwords.matches("correct-password", "stored-hash")).thenReturn(true);
        when(users.save(any())).thenAnswer(call -> call.getArgument(0));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(Jwt.withTokenValue("signed-token")
                .header("alg", "HS256").subject("user-sub").issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600)).build());

        LoginResponse response = service("", "").login(new LoginRequest("STUDENT@example.edu", "correct-password"));
        assertEquals("signed-token", response.accessToken());
        assertEquals("user-sub", response.user().subject());
    }

    @Test void studentAccountRequiresAnExistingRecordAndGetsOnlyTheStudentRole() {
        Student student = mock(Student.class);
        when(student.getFirstName()).thenReturn("Amira"); when(student.getLastName()).thenReturn("Ben Ali");
        when(student.getId()).thenReturn(1L);
        when(students.findByEmailIgnoreCase("amira@example.edu")).thenReturn(Optional.of(student));
        when(students.findById(1L)).thenReturn(Optional.of(student));
        when(users.findByEmailIgnoreCase("amira@example.edu")).thenReturn(Optional.empty());
        when(passwords.encode(anyString())).thenReturn("bcrypt-hash");
        when(users.save(any())).thenAnswer(call -> call.getArgument(0));

        StudentCredentials credentials = service("", "").createStudentAccount(
                new StudentAccountRequest("amira@example.edu"));
        assertEquals("amira@example.edu", credentials.user().email());
        assertEquals(java.util.Set.of(AppRole.STUDENT), credentials.user().roles());
        assertEquals(20, credentials.temporaryPassword().length());
        verify(passwords).encode(credentials.temporaryPassword());
    }

    @Test void incorrectPasswordUsesTheSameGenericCredentialError() {
        AppUser user = new AppUser("user-sub", "student@example.edu", "Student User");
        user.setPasswordHash("stored-hash");
        when(users.findByEmailIgnoreCase("student@example.edu")).thenReturn(Optional.of(user));
        when(passwords.matches("wrong", "stored-hash")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class,
                () -> service("", "").login(new LoginRequest("student@example.edu", "wrong")));
        verifyNoInteractions(jwtEncoder);
    }
}
