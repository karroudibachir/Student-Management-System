package com.sms.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student,Long>, JpaSpecificationExecutor<Student> {
 boolean existsByEmailIgnoreCase(String email);
 boolean existsByEmailIgnoreCaseAndIdNot(String email,Long id);
 @Query("select distinct s.major from Student s order by s.major") List<String> findDistinctMajors();
 long countByStatus(StudentStatus status);
 @Query("select coalesce(avg(s.gpa),0) from Student s") Double averageGpa();
}
