package com.sms.student;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional
public class StudentServiceImpl implements StudentService {
 private final StudentRepository repository;
 public StudentServiceImpl(StudentRepository repository){this.repository=repository;}
 public StudentResponse create(StudentRequest r){if(repository.existsByEmailIgnoreCase(r.email()))throw new DuplicateEmailException();Student s=new Student();apply(s,r);s.setStudentCode("TMP-"+UUID.randomUUID().toString().substring(0,12));Student saved=repository.saveAndFlush(s);saved.setStudentCode(String.format("STU-%d-%04d",saved.getEnrollmentYear(),saved.getId()));return StudentResponse.from(repository.save(saved));}
 @Transactional(readOnly=true) public StudentResponse get(Long id){return StudentResponse.from(find(id));}
 @Transactional(readOnly=true) public Page<StudentResponse> list(String q,String major,StudentStatus status,Integer year,Pageable p){Specification<Student> spec=Specification.where(null);if(q!=null&&!q.isBlank()){String term="%"+q.trim().toLowerCase(Locale.ROOT)+"%";spec=spec.and((root,query,cb)->cb.or(cb.like(cb.lower(root.get("firstName")),term),cb.like(cb.lower(root.get("lastName")),term),cb.like(cb.lower(root.get("email")),term),cb.like(cb.lower(root.get("studentCode")),term)));}if(major!=null&&!major.isBlank())spec=spec.and((root,query,cb)->cb.equal(root.get("major"),major));if(status!=null)spec=spec.and((root,query,cb)->cb.equal(root.get("status"),status));if(year!=null)spec=spec.and((root,query,cb)->cb.equal(root.get("enrollmentYear"),year));return repository.findAll(spec,p).map(StudentResponse::from);}
 public StudentResponse update(Long id,StudentRequest r){Student s=find(id);if(repository.existsByEmailIgnoreCaseAndIdNot(r.email(),id))throw new DuplicateEmailException();apply(s,r);return StudentResponse.from(repository.save(s));}
 @Transactional(readOnly=true) public StudentResponse getOwnProfile(Long studentId){return StudentResponse.from(find(studentId));}
 public StudentResponse updateOwnProfile(Long studentId,StudentProfileRequest r){Student s=find(studentId);s.setFirstName(r.firstName().trim());s.setLastName(r.lastName().trim());s.setPhone(r.phone());s.setDateOfBirth(r.dateOfBirth());s.setGender(r.gender());s.setAddress(r.address());s.setPhotoUrl(r.photoUrl());return StudentResponse.from(repository.save(s));}
 public void delete(Long id){repository.delete(find(id));}
 @Transactional(readOnly=true) public List<String> majors(){return repository.findDistinctMajors();}
 @Transactional(readOnly=true) public Map<String,Object> stats(){return Map.of("total",repository.count(),"active",repository.countByStatus(StudentStatus.ACTIVE),"graduated",repository.countByStatus(StudentStatus.GRADUATED),"suspended",repository.countByStatus(StudentStatus.SUSPENDED),"averageGpa",repository.averageGpa());}
 private Student find(Long id){return repository.findById(id).orElseThrow(()->new ResourceNotFoundException(id));}
 private void apply(Student s,StudentRequest r){s.setFirstName(r.firstName().trim());s.setLastName(r.lastName().trim());s.setEmail(r.email().trim().toLowerCase(Locale.ROOT));s.setPhone(r.phone());s.setDateOfBirth(r.dateOfBirth());s.setGender(r.gender());s.setAddress(r.address());s.setMajor(r.major().trim());s.setEnrollmentYear(r.enrollmentYear());s.setGpa(r.gpa());s.setStatus(r.status());s.setPhotoUrl(r.photoUrl());}
}
