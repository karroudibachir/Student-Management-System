package com.sms.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BootstrapConfiguration {
    @Bean CommandLineRunner bootstrapAdmin(AppUserService users) { return args -> users.bootstrapAdmin(); }
}
