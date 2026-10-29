package com.sms.student;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record StudentResponse(Long id,String studentCode,String firstName,String lastName,String email,String phone,
 LocalDate dateOfBirth,Gender gender,String address,String major,Integer enrollmentYear,BigDecimal gpa,
 StudentStatus status,String photoUrl,OffsetDateTime createdAt,OffsetDateTime updatedAt) {
 static StudentResponse from(Student s){return new StudentResponse(s.getId(),s.getStudentCode(),s.getFirstName(),s.getLastName(),s.getEmail(),s.getPhone(),s.getDateOfBirth(),s.getGender(),s.getAddress(),s.getMajor(),s.getEnrollmentYear(),s.getGpa(),s.getStatus(),s.getPhotoUrl(),s.getCreatedAt(),s.getUpdatedAt());}
}
