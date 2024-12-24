package com.c0324.casestudym5.service;


import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.dto.StudentSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


public interface IStudentService {
    Page<Student> getPageStudents(Pageable pageable, StudentSearchDTO search);
    List<Student> getStudents(StudentSearchDTO search);
    Student getStudent(Long id);
}
