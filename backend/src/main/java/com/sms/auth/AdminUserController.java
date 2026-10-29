package com.sms.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final AppUserService users;
    public AdminUserController(AppUserService users) { this.users = users; }
    @GetMapping public List<AppUserView> list() { return users.listUsers(); }
    @PostMapping("/student-accounts")
    public AppUserView createStudentAccount(@Valid @RequestBody StudentAccountRequest request) {
        return users.createStudentAccount(request);
    }
    @PostMapping("/admin-accounts")
    public AppUserView createAdminAccount(@Valid @RequestBody AdminAccountRequest request) {
        return users.createAdminAccount(request);
    }
    @DeleteMapping("/{subject}")
    public void deleteStudentAccount(@PathVariable String subject) { users.deleteStudentAccount(subject); }
}
