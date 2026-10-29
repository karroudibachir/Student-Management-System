package com.sms.student;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record StudentRequest(@NotBlank @Size(max=80) String firstName,@NotBlank @Size(max=80) String lastName,
 @NotBlank @Email @Size(max=254) String email,@Size(max=32) String phone,@NotNull @Past LocalDate dateOfBirth,
 Gender gender,@Size(max=500) String address,@NotBlank @Size(max=100) String major,@NotNull @Min(1900) @Max(2200) Integer enrollmentYear,
 @DecimalMin("0.00") @DecimalMax("4.00") @Digits(integer=1,fraction=2) BigDecimal gpa,@NotNull StudentStatus status,@Size(max=2048) String photoUrl) {}
