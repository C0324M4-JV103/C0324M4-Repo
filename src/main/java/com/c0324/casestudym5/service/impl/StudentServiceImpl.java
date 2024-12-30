package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import com.c0324.casestudym5.repository.StudentRepository;
import com.c0324.casestudym5.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public Student findStudentByUserId(Long id) {
        return studentRepository.findStudentByUserId(id);
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public void save(Student student) {
        studentRepository.save(student);
    }

    @Override
    public Student findById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Student> findAllExceptCurrentStudent(Long currentStudentId) {
        return studentRepository.findAllExceptCurrentStudent(currentStudentId);
    }

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
    @Override
    public String getStudentEmailById(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        return student.getUser().getEmail();
    }
}
