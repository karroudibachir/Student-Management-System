package com.sms.student;

import com.sms.auth.AppRole;
import com.sms.auth.AppUser;
import com.sms.auth.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest {
    @Autowired StudentRepository repository;
    @Autowired AppUserRepository users;

    @Test void persistsStudentAndSupportsSearchAndDistinctMajorQueries() {
        Student s = new Student();
        s.setStudentCode("STU-2024-0001");
        s.setFirstName("Amira"); s.setLastName("Ben Ali"); s.setEmail("amira@example.edu");
        s.setDateOfBirth(LocalDate.of(2004, 3, 14)); s.setMajor("Computer Science");
        s.setEnrollmentYear(2024); s.setGpa(new BigDecimal("3.82")); s.setStatus(StudentStatus.ACTIVE);
        repository.saveAndFlush(s);

        org.springframework.data.jpa.domain.Specification<Student> search =
                (root, query, cb) -> cb.like(cb.lower(root.get("email")), "%amira%");
        assertEquals(1, repository.findAll(search).size());
        assertTrue(repository.existsByEmailIgnoreCase("AMIRA@example.edu"));
        assertEquals(java.util.List.of("Computer Science"), repository.findDistinctMajors());
    }

    @Test void persistsWorkspaceRolesAndCountsAdministrators() {
        AppUser user = new AppUser("subject-1", "admin@example.edu", "Workspace Admin");
        user.getRoles().add(AppRole.ADMIN);
        users.saveAndFlush(user);
        assertEquals(1, users.countUsersWithRole(AppRole.ADMIN));
        assertEquals(java.util.List.of("Workspace Admin"), users.findAllByOrderByDisplayNameAsc().stream()
                .map(AppUser::getDisplayName).toList());
    }

}
