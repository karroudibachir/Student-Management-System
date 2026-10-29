package com.sms.auth;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record RoleUpdateRequest(@NotEmpty Set<AppRole> roles) {}
