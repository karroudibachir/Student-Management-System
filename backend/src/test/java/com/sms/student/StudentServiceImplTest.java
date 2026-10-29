package com.sms.student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) class StudentServiceImplTest {
 @Mock StudentRepository repository;
 @InjectMocks StudentServiceImpl service;
 StudentRequest request(String email){return new StudentRequest("Ada","Lovelace",email,null,LocalDate.of(2000,1,1),Gender.FEMALE,"London","Mathematics",2024,new BigDecimal("3.90"),StudentStatus.ACTIVE,null);}
 @Test void createRejectsDuplicateEmail(){when(repository.existsByEmailIgnoreCase("ada@example.edu")).thenReturn(true);assertThrows(DuplicateEmailException.class,()->service.create(request("ada@example.edu")));verify(repository,never()).save(any());}
 @Test void createAssignsGeneratedStudentCode(){when(repository.existsByEmailIgnoreCase("ada@example.edu")).thenReturn(false);when(repository.saveAndFlush(any(Student.class))).thenAnswer(call->call.getArgument(0));when(repository.save(any(Student.class))).thenAnswer(call->call.getArgument(0));StudentResponse created=service.create(request("ada@example.edu"));assertTrue(created.studentCode().startsWith("STU-2024-"));verify(repository).saveAndFlush(any(Student.class));}
 @Test void getRejectsMissingStudent(){when(repository.findById(9L)).thenReturn(Optional.empty());assertThrows(ResourceNotFoundException.class,()->service.get(9L));}
 @Test void updateRejectsEmailOwnedByAnotherStudent(){when(repository.findById(1L)).thenReturn(Optional.of(new Student()));when(repository.existsByEmailIgnoreCaseAndIdNot("ada@example.edu",1L)).thenReturn(true);assertThrows(DuplicateEmailException.class,()->service.update(1L,request("ada@example.edu")));}
 @Test void updatePersistsValidatedChanges(){Student existing=new Student();when(repository.findById(1L)).thenReturn(Optional.of(existing));when(repository.existsByEmailIgnoreCaseAndIdNot("ada@example.edu",1L)).thenReturn(false);when(repository.save(existing)).thenReturn(existing);StudentResponse updated=service.update(1L,request("ada@example.edu"));assertEquals("Ada",updated.firstName());assertEquals("ada@example.edu",updated.email());}
 @Test void deleteRejectsMissingStudent(){when(repository.findById(9L)).thenReturn(Optional.empty());assertThrows(ResourceNotFoundException.class,()->service.delete(9L));verify(repository,never()).delete(any(Student.class));}
 @Test void deleteRemovesExistingStudent(){Student existing=new Student();when(repository.findById(1L)).thenReturn(Optional.of(existing));service.delete(1L);verify(repository).delete(existing);}
 @Test void listAppliesSupportedFilters(){when(repository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Student>>any(),any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));var result=service.list("ada","Mathematics",StudentStatus.ACTIVE,2024,PageRequest.of(0,5));assertTrue(result.isEmpty());verify(repository).findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Student>>any(),any(Pageable.class));}
 @Test void exposesMajorsAndDashboardStats(){when(repository.findDistinctMajors()).thenReturn(List.of("Mathematics"));when(repository.count()).thenReturn(2L);when(repository.countByStatus(StudentStatus.ACTIVE)).thenReturn(1L);when(repository.countByStatus(StudentStatus.GRADUATED)).thenReturn(1L);when(repository.countByStatus(StudentStatus.SUSPENDED)).thenReturn(0L);when(repository.averageGpa()).thenReturn(3.5);assertEquals(List.of("Mathematics"),service.majors());Map<String,Object> stats=service.stats();assertEquals(2L,stats.get("total"));assertEquals(3.5,stats.get("averageGpa"));}
}
