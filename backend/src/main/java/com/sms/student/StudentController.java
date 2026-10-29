package com.sms.student;

import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import com.sms.auth.AppUserService;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.*;

@RestController @RequestMapping("/api/v1/students")
public class StudentController {
 private final StudentService service; private final AppUserService accounts; public StudentController(StudentService service,AppUserService accounts){this.service=service;this.accounts=accounts;}
 @GetMapping("/me") public StudentResponse myProfile(@AuthenticationPrincipal Jwt jwt){return service.getOwnProfile(accounts.studentIdFor(jwt.getSubject()));}
 @PutMapping("/me") public StudentResponse updateMyProfile(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody StudentProfileRequest request){StudentResponse updated=service.updateOwnProfile(accounts.studentIdFor(jwt.getSubject()),request);accounts.updateStudentDisplayName(jwt.getSubject(),updated.firstName()+" "+updated.lastName());return updated;}
 @PostMapping public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request){StudentResponse s=service.create(request);return ResponseEntity.created(URI.create("/api/v1/students/"+s.id())).body(s);}
 @GetMapping public Page<StudentResponse> list(@RequestParam(required=false) String q,@RequestParam(required=false) String major,@RequestParam(required=false) StudentStatus status,@RequestParam(required=false) Integer year,@PageableDefault(size=10,sort="lastName") Pageable pageable){return service.list(q,major,status,year,pageable);}
 @GetMapping("/{id}") public StudentResponse get(@PathVariable Long id){return service.get(id);}
 @PutMapping("/{id}") @Transactional public StudentResponse update(@PathVariable Long id,@Valid @RequestBody StudentRequest request){StudentResponse updated=service.update(id,request);accounts.syncStudentAccount(id,updated.email(),updated.firstName()+" "+updated.lastName());return updated;}
 @DeleteMapping("/{id}") @Transactional public ResponseEntity<Void> delete(@PathVariable Long id){accounts.deleteStudentAccountForStudent(id);service.delete(id);return ResponseEntity.noContent().build();}
 @GetMapping("/stats") public Map<String,Object> stats(){return service.stats();}
 @GetMapping("/majors") public List<String> majors(){return service.majors();}
}
