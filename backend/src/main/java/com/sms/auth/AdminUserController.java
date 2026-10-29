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
    @PutMapping("/{subject}/roles")
    public AppUserView updateRoles(@PathVariable String subject, @Valid @RequestBody RoleUpdateRequest request) {
        return users.updateRoles(subject, request.roles());
    }
}
