package com.sms.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final AppUserService users;
    private final StudentEmailService email;
    public AdminUserController(AppUserService users, StudentEmailService email) { this.users = users; this.email = email; }
    @GetMapping public List<AppUserView> list() { return users.listUsers(); }
    @PostMapping("/student-accounts")
    @Transactional
    public AppUserView createStudentAccount(@Valid @RequestBody StudentAccountRequest request) {
        email.ensureConfigured();
        StudentCredentials credentials = users.createStudentAccount(request);
        email.sendCredentialsForExistingRecord(request.email(), credentials);
        return credentials.user();
    }
    @PostMapping("/admin-accounts")
    public AppUserView createAdminAccount(@Valid @RequestBody AdminAccountRequest request) {
        return users.createAdminAccount(request);
    }
    @DeleteMapping("/{subject}")
    public void deleteStudentAccount(@PathVariable String subject) { users.deleteStudentAccount(subject); }
}
