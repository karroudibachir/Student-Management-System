package com.sms.student;

import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.*;

@RestController @RequestMapping("/api/v1/students")
public class StudentController {
 private final StudentService service; public StudentController(StudentService service){this.service=service;}
 @PostMapping public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request){StudentResponse s=service.create(request);return ResponseEntity.created(URI.create("/api/v1/students/"+s.id())).body(s);}
 @GetMapping public Page<StudentResponse> list(@RequestParam(required=false) String q,@RequestParam(required=false) String major,@RequestParam(required=false) StudentStatus status,@RequestParam(required=false) Integer year,@PageableDefault(size=10,sort="lastName") Pageable pageable){return service.list(q,major,status,year,pageable);}
 @GetMapping("/{id}") public StudentResponse get(@PathVariable Long id){return service.get(id);}
 @PutMapping("/{id}") public StudentResponse update(@PathVariable Long id,@Valid @RequestBody StudentRequest request){return service.update(id,request);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){service.delete(id);return ResponseEntity.noContent().build();}
 @GetMapping("/stats") public Map<String,Object> stats(){return service.stats();}
 @GetMapping("/majors") public List<String> majors(){return service.majors();}
}
