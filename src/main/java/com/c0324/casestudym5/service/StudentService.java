package com.c0324.casestudym5.service;


import com.c0324.casestudym5.dto.StudentDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface StudentService {
    Page<Student> getPageStudents(Pageable pageable, StudentSearchDTO search);
    List<Student> getStudents(StudentSearchDTO search);
    Student getStudent(Long id);
    Student getStudentByUserEmail(String email);
    void save(Student student);
    Student findById(Long id);
    Student findStudentByUserId(Long id);
    String getStudentEmailById(Long id);
    Page<Student> findAllExceptCurrentStudent(Long currentStudentId, Pageable pageable);
    Page<Student> searchStudentsExceptCurrent(String search, Long id, Pageable pageable);
    void createNewStudent(StudentDTO studentDTO, MultipartFile avatar) throws Exception;
    void editStudent(Long id, StudentDTO studentDTO, MultipartFile avatar) throws Exception;
    void deleteStudentById(Long id) throws Exception;
}
