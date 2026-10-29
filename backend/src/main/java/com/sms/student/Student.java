package com.sms.student;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity @Table(name="students", indexes={@Index(name="idx_student_major",columnList="major"),@Index(name="idx_student_status",columnList="status")})
public class Student {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=24) private String studentCode;
    @Column(nullable=false,length=80) private String firstName;
    @Column(nullable=false,length=80) private String lastName;
    @Column(nullable=false,unique=true,length=254) private String email;
    @Column(length=32) private String phone;
    @Column(nullable=false) private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING) private Gender gender;
    @Column(length=500) private String address;
    @Column(nullable=false,length=100) private String major;
    @Column(nullable=false) private Integer enrollmentYear;
    @Column(precision=3,scale=2) private BigDecimal gpa;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private StudentStatus status;
    @Column(length=2048) private String photoUrl;
    @Column(nullable=false,updatable=false) private OffsetDateTime createdAt;
    @Column(nullable=false) private OffsetDateTime updatedAt;
    protected Student() {}
    @PrePersist void createTimestamps(){var now=OffsetDateTime.now();createdAt=now;updatedAt=now;}
    @PreUpdate void updateTimestamp(){updatedAt=OffsetDateTime.now();}
    public Long getId(){return id;} public String getStudentCode(){return studentCode;} public void setStudentCode(String v){studentCode=v;}
    public String getFirstName(){return firstName;} public void setFirstName(String v){firstName=v;} public String getLastName(){return lastName;} public void setLastName(String v){lastName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public LocalDate getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(LocalDate v){dateOfBirth=v;} public Gender getGender(){return gender;} public void setGender(Gender v){gender=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;} public String getMajor(){return major;} public void setMajor(String v){major=v;}
    public Integer getEnrollmentYear(){return enrollmentYear;} public void setEnrollmentYear(Integer v){enrollmentYear=v;} public BigDecimal getGpa(){return gpa;} public void setGpa(BigDecimal v){gpa=v;}
    public StudentStatus getStatus(){return status;} public void setStatus(StudentStatus v){status=v;} public String getPhotoUrl(){return photoUrl;} public void setPhotoUrl(String v){photoUrl=v;}
    public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
