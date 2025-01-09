package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Faculty findByName(String name);
}
