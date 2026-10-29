package com.sms.auth;

import com.sms.student.Student;
import com.sms.student.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class AppUserService {
    private final AppUserRepository users;
    private final StudentRepository students;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String bootstrapAdminEmail;
    private final String bootstrapAdminPassword;

    public AppUserService(AppUserRepository users, StudentRepository students, PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder, @Value("${SMS_BOOTSTRAP_ADMIN_EMAIL:}") String bootstrapAdminEmail,
            @Value("${SMS_BOOTSTRAP_ADMIN_PASSWORD:}") String bootstrapAdminPassword) {
        this.users = users;
        this.students = students;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.bootstrapAdminEmail = normalize(bootstrapAdminEmail);
        this.bootstrapAdminPassword = bootstrapAdminPassword;
    }

    public void bootstrapAdmin() {
        if (bootstrapAdminEmail.isBlank() && (bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank())) return;
        if (bootstrapAdminEmail.isBlank() || bootstrapAdminPassword == null
                || bootstrapAdminPassword.length() < 12 || bootstrapAdminPassword.length() > 72
                || bootstrapAdminPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Configure both bootstrap admin values and use a password of 12 to 72 characters");
        }
        AppUser admin = users.findByEmailIgnoreCase(bootstrapAdminEmail)
                .orElseGet(() -> new AppUser(UUID.randomUUID().toString(), bootstrapAdminEmail, "Administrator"));
        if (admin.getPasswordHash() == null) admin.setPasswordHash(passwordEncoder.encode(bootstrapAdminPassword));
        admin.getRoles().clear();
        admin.getRoles().add(AppRole.ADMIN);
        users.save(admin);
    }

    public LoginResponse login(LoginRequest request) {
        String email = normalize(request.email());
        AppUser user = users.findByEmailIgnoreCase(email).orElseThrow(InvalidCredentialsException::new);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("student-management").subject(user.getSubject())
                .issuedAt(now).expiresAt(now.plus(8, ChronoUnit.HOURS))
                .claim("email", user.getEmail()).claim("name", user.getDisplayName()).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
        user.touch();
        return new LoginResponse(token, AppUserView.from(users.save(user)));
    }

    @Transactional(readOnly = true)
    public AppUserView currentUser(String subject) {
        return users.findById(subject).map(AppUserView::from).orElseThrow(InvalidCredentialsException::new);
    }

    @Transactional(readOnly = true)
    public Long studentIdFor(String subject) {
        AppUser user = users.findById(subject).orElseThrow(InvalidCredentialsException::new);
        if (user.getStudentId() == null) throw new InvalidCredentialsException();
        return user.getStudentId();
    }

    public void updateStudentDisplayName(String subject, String displayName) {
        AppUser user = users.findById(subject).orElseThrow(InvalidCredentialsException::new);
        if (!user.getRoles().contains(AppRole.STUDENT)) throw new InvalidCredentialsException();
        user.setDisplayName(displayName);
        users.save(user);
    }

    public void syncStudentAccount(Long studentId, String email, String displayName) {
        users.findByStudentId(studentId).ifPresent(user -> {
            String normalizedEmail = normalize(email);
            users.findByEmailIgnoreCase(normalizedEmail).filter(other -> !other.getSubject().equals(user.getSubject()))
                    .ifPresent(other -> { throw new IllegalArgumentException("An account already exists for this email"); });
            user.setEmail(normalizedEmail);
            user.setDisplayName(displayName);
            users.save(user);
        });
    }

    public void deleteStudentAccountForStudent(Long studentId) {
        users.findByStudentId(studentId).ifPresent(users::delete);
    }

    @Transactional(readOnly = true)
    public List<AppUserView> listUsers() {
        return users.findAllByOrderByDisplayNameAsc().stream().map(AppUserView::from).toList();
    }

    public AppUserView createStudentAccount(StudentAccountRequest request) {
        String email = normalize(request.email());
        validatePasswordLength(request.initialPassword());
        Student student = students.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Create the student record before creating its account"));
        AppUser user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user != null && user.getRoles().contains(AppRole.ADMIN)) {
            throw new IllegalArgumentException("An administrator account cannot be converted to a student account");
        }
        if (user != null && user.getPasswordHash() != null) {
            throw new IllegalArgumentException("An account already exists for this student");
        }
        if (user == null) user = new AppUser(UUID.randomUUID().toString(), email,
                student.getFirstName() + " " + student.getLastName());
        user.setEmail(email);
        user.setDisplayName(student.getFirstName() + " " + student.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.initialPassword()));
        user.setRoles(Set.of(AppRole.STUDENT));
        user.setStudentId(student.getId());
        return AppUserView.from(users.save(user));
    }

    public AppUserView createAdminAccount(AdminAccountRequest request) {
        String email = normalize(request.email());
        validatePasswordLength(request.initialPassword());
        if (students.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Use a student account for emails linked to student records");
        }
        if (users.findByEmailIgnoreCase(email).filter(user -> user.getPasswordHash() != null).isPresent()) {
            throw new IllegalArgumentException("An account already exists for this email");
        }
        AppUser user = users.findByEmailIgnoreCase(email)
                .orElseGet(() -> new AppUser(UUID.randomUUID().toString(), email, email));
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.initialPassword()));
        user.setRoles(Set.of(AppRole.ADMIN));
        return AppUserView.from(users.save(user));
    }

    public void deleteStudentAccount(String subject) {
        AppUser user = users.findById(subject).orElseThrow(InvalidCredentialsException::new);
        if (user.getRoles().contains(AppRole.ADMIN)) {
            throw new IllegalArgumentException("Administrator accounts cannot be removed here");
        }
        users.delete(user);
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Passwords must be no longer than 72 UTF-8 bytes");
        }
    }
}
