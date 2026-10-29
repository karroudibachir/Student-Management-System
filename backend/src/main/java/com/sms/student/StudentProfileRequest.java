package com.sms.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record StudentProfileRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 32) String phone,
        @NotNull @Past LocalDate dateOfBirth,
        Gender gender,
        @Size(max = 500) String address,
        @Size(max = 2048) String photoUrl) {}
