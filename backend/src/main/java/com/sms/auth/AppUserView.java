package com.sms.auth;

import java.util.Set;

public record AppUserView(String subject, String email, String displayName, Set<AppRole> roles) {
    static AppUserView from(AppUser user) {
        return new AppUserView(user.getSubject(), user.getEmail(), user.getDisplayName(), Set.copyOf(user.getRoles()));
    }
}
