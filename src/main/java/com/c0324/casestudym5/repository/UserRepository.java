package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
