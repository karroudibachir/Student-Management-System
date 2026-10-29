package com.sms.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
    List<AppUser> findAllByOrderByDisplayNameAsc();
    Optional<AppUser> findByEmailIgnoreCase(String email);
    Optional<AppUser> findByStudentId(Long studentId);
    @Query("select count(distinct u) from AppUser u join u.roles r where r = :role")
    long countUsersWithRole(AppRole role);
}
