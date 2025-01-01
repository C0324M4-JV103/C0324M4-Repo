package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface StudentService {
    Page<Student> getPageStudents(Pageable pageable, StudentSearchDTO search);
    List<Student> getStudents(StudentSearchDTO search);
    Student getStudent(Long id);
    void save(Student student);
    Student findById(Long id);
    Student findStudentByUserId(Long id);
    String getStudentEmailById(Long id);
    Page<Student> getAvailableStudents(int page, String search, Long currentStudentId);
    Page<Student> findAllExceptCurrentStudent(Long id, Pageable pageable);
}
