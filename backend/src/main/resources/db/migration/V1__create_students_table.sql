CREATE TABLE students (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 student_code VARCHAR(24) NOT NULL,
 first_name VARCHAR(80) NOT NULL,
 last_name VARCHAR(80) NOT NULL,
 email VARCHAR(254) NOT NULL,
 phone VARCHAR(32),
 date_of_birth DATE NOT NULL,
 gender VARCHAR(16),
 address VARCHAR(500),
 major VARCHAR(100) NOT NULL,
 enrollment_year INT NOT NULL,
 gpa DECIMAL(3,2),
 status VARCHAR(16) NOT NULL,
 photo_url VARCHAR(2048),
 created_at TIMESTAMP(6) NOT NULL,
 updated_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT uk_students_student_code UNIQUE (student_code),
 CONSTRAINT uk_students_email UNIQUE (email)
);
CREATE INDEX idx_students_major ON students(major);
CREATE INDEX idx_students_status ON students(status);
