package com.sms.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface StudentService {
 StudentResponse create(StudentRequest request); StudentResponse get(Long id);
 Page<StudentResponse> list(String q,String major,StudentStatus status,Integer year,Pageable pageable);
 StudentResponse update(Long id,StudentRequest request); StudentResponse getOwnProfile(Long studentId);
 StudentResponse updateOwnProfile(Long studentId,StudentProfileRequest request);
 void delete(Long id); List<String> majors(); Map<String,Object> stats();
}
