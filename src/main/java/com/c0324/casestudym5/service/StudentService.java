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
    Student findStudentByUserId(Long id);
    List<Student> findAll();
    void save(Student student);
    Student findById(Long id);
    Page<Student> findAllExceptCurrentStudent(Long currentStudentId, Pageable pageable);
    String getStudentEmailById(Long id);
    Page<Student> searchStudentsExceptCurrent(String search, Long id, Pageable pageable);

}
