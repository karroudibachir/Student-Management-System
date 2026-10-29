package com.sms.student;

import com.sms.student.StudentStatus;
import com.sms.auth.AppUserRepository;
import com.sms.auth.AppUserService;
import com.sms.auth.StudentCredentials;
import com.sms.auth.StudentEmailService;
import com.sms.auth.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@Import({ApiExceptionHandler.class, SecurityConfiguration.class})
class StudentControllerTest {
    @Autowired MockMvc mvc;
    @MockBean StudentService service;
    @MockBean AppUserRepository users;
    @MockBean AppUserService accounts;
    @MockBean StudentEmailService email;

    private StudentResponse response() {
        var now = OffsetDateTime.now();
        return new StudentResponse(1L, "STU-2024-0001", "Ada", "Lovelace", "ada@example.edu",
                null, LocalDate.of(2000, 1, 1), Gender.FEMALE, null, "Mathematics", 2024,
                new BigDecimal("3.90"), StudentStatus.ACTIVE, null, now, now);
    }

    @Test @WithMockUser(roles = "ADMIN") void createReturnsLocationAndDto() throws Exception {
        when(service.create(any())).thenReturn(response());
        when(accounts.createStudentAccountFor(any(), any(), any())).thenReturn(new StudentCredentials(null, "temporary-password"));
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content("""
                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.edu",
                 "dateOfBirth":"2000-01-01","gender":"FEMALE","major":"Mathematics",
                 "enrollmentYear":2024,"gpa":3.90,"status":"ACTIVE"}
                """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/students/1"))
            .andExpect(jsonPath("$.studentCode").value("STU-2024-0001"));
    }

    @Test @WithMockUser(roles = "ADMIN") void invalidRequestReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\",\"email\":\"not-an-email\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/v1/students"))
            .andExpect(jsonPath("$.fieldErrors.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test @WithMockUser(roles = "ADMIN") void duplicateEmailReturnsConflict() throws Exception {
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

    @Test @WithMockUser(roles = "ADMIN") void missingStudentReturnsConsistentNotFoundBody() throws Exception {
        when(service.get(42L)).thenThrow(new ResourceNotFoundException(42L));
        mvc.perform(get("/api/v1/students/42"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Student with id 42 was not found"))
            .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test @WithMockUser(roles = "STUDENT") void studentCannotCreateStudentRecords() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "STUDENT") void studentCannotDeleteStudentRecords() throws Exception {
        mvc.perform(delete("/api/v1/students/1")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "STUDENT") void studentCannotReadAnotherStudentById() throws Exception {
        mvc.perform(get("/api/v1/students/1")).andExpect(status().isForbidden());
    }

    @Test void studentCanReadOnlyTheProfileLinkedToTheirAccount() throws Exception {
        when(accounts.studentIdFor("student-sub")).thenReturn(1L);
        when(service.getOwnProfile(1L)).thenReturn(response());
        mvc.perform(get("/api/v1/students/me").with(jwt().jwt(token -> token.subject("student-sub"))
                        .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.studentCode").value("STU-2024-0001"));
    }

    @Test void studentCanUpdateOnlyPersonalFieldsOnTheirOwnProfile() throws Exception {
        when(accounts.studentIdFor("student-sub")).thenReturn(1L);
        when(service.updateOwnProfile(org.mockito.ArgumentMatchers.eq(1L), any())).thenReturn(response());
        mvc.perform(put("/api/v1/students/me").with(jwt().jwt(token -> token.subject("student-sub"))
                        .authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName":"Ada","lastName":"Lovelace","phone":"+216 20 000 000",
                         "dateOfBirth":"2000-01-01","gender":"FEMALE","address":"Tunis",
                         "photoUrl":"https://example.edu/ada.png"}
                        """))
            .andExpect(status().isOk());
    }
}

