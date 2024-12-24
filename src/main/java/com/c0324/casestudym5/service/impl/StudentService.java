package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.dto.StudentSearchDTO;
import com.c0324.casestudym5.repository.IStudentRepository;
import com.c0324.casestudym5.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService implements IStudentService {
    @Autowired
    IStudentRepository studentRepository;

    @Override
    public Page<Student> getPageStudents(Pageable pageable, StudentSearchDTO search) {
        return studentRepository.getPageStudents(pageable, search.getEmail(), search.getName(), search.getClazzId());
    }

    @Override
    public List<Student> getStudents(StudentSearchDTO search) {
        return studentRepository.getStudents(search.getEmail(), search.getName(), search.getClazzId());
    }

    @Override
    public Student getStudent(Long id) {
        return studentRepository.findById(id).get();
    }
}