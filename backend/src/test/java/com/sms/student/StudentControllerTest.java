package com.sms.student;

import com.sms.student.StudentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@Import(ApiExceptionHandler.class)
class StudentControllerTest {
    @Autowired MockMvc mvc;
    @MockBean StudentService service;

    private StudentResponse response() {
        var now = OffsetDateTime.now();
        return new StudentResponse(1L, "STU-2024-0001", "Ada", "Lovelace", "ada@example.edu",
                null, LocalDate.of(2000, 1, 1), Gender.FEMALE, null, "Mathematics", 2024,
                new BigDecimal("3.90"), StudentStatus.ACTIVE, null, now, now);
    }

    @Test void createReturnsLocationAndDto() throws Exception {
        when(service.create(any())).thenReturn(response());
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content("""
                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.edu",
                 "dateOfBirth":"2000-01-01","gender":"FEMALE","major":"Mathematics",
                 "enrollmentYear":2024,"gpa":3.90,"status":"ACTIVE"}
                """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/students/1"))
            .andExpect(jsonPath("$.studentCode").value("STU-2024-0001"));
    }

    @Test void invalidRequestReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\",\"email\":\"not-an-email\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/v1/students"))
            .andExpect(jsonPath("$.fieldErrors.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test void duplicateEmailReturnsConflict() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateEmailException());
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content("""
                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.edu",
                 "dateOfBirth":"2000-01-01","major":"Mathematics","enrollmentYear":2024,
                 "status":"ACTIVE"}
                """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("A student with this email already exists"));
    }

    @Test void missingStudentReturnsConsistentNotFoundBody() throws Exception {
        when(service.get(42L)).thenThrow(new ResourceNotFoundException(42L));
        mvc.perform(get("/api/v1/students/42"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Student with id 42 was not found"))
            .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
