package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
