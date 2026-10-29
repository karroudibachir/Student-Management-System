package com.sms.auth;

public record LoginResponse(String accessToken, AppUserView user) {}
