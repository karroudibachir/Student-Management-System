package com.sms.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class AppUserService {
    private final AppUserRepository users;
    private final String bootstrapAdminEmail;

    public AppUserService(AppUserRepository users,
            @Value("${SMS_BOOTSTRAP_ADMIN_EMAIL:}") String bootstrapAdminEmail) {
        this.users = users;
        this.bootstrapAdminEmail = bootstrapAdminEmail.trim().toLowerCase(Locale.ROOT);
    }

    public AppUserView currentUser(Jwt jwt) {
        String subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String displayName = jwt.getClaimAsString("name");
        if (displayName == null || displayName.isBlank()) displayName = jwt.getClaimAsString("preferred_username");
        final String display = displayName;
        AppUser user = users.findById(subject).orElseGet(() -> new AppUser(subject, email, display));
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.touch();
        if (!bootstrapAdminEmail.isEmpty() && email != null
                && bootstrapAdminEmail.equals(email.toLowerCase(Locale.ROOT))) user.getRoles().add(AppRole.ADMIN);
        if (user.getRoles().isEmpty()) user.getRoles().add(AppRole.VIEWER);
        return AppUserView.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public List<AppUserView> listUsers() {
        return users.findAllByOrderByDisplayNameAsc().stream().map(AppUserView::from).toList();
    }

    public AppUserView updateRoles(String subject, Set<AppRole> roles) {
        AppUser user = users.findById(subject).orElseThrow(() -> new IllegalArgumentException("User has not signed in yet"));
        if (user.getRoles().contains(AppRole.ADMIN) && !roles.contains(AppRole.ADMIN)
                && users.countUsersWithRole(AppRole.ADMIN) <= 1) throw new LastAdministratorException();
        user.setRoles(roles);
        return AppUserView.from(users.save(user));
    }
}
