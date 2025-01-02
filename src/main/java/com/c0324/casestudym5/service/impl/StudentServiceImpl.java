package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.repository.StudentRepository;
import com.c0324.casestudym5.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public Page<Student> getPageStudents(Pageable pageable, StudentSearchDTO search) {
        return studentRepository.getPageStudents(pageable, search.getEmail(), search.getName(), search.getClazzId());
    }
    @Override
    public List<Student> getStudents(StudentSearchDTO search) {
        return studentRepository.getStudents(search.getEmail(), search.getName(), search.getClazzId());
    }
    @Override
    public Student getStudent(Long id) {
        return studentRepository.findById(id).orElse(null);
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
    public Student findStudentByUserId(Long id) {
        return studentRepository.findStudentByUserId(id);
    }

    @Override
    public String getStudentEmailById(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        return student.getUser().getEmail();
    }

    @Override
    public Page<Student> getAvailableStudents(int page, String search, Long currentStudentId) {
        Pageable pageable = PageRequest.of(page - 1, 5);
        if (search != null && !search.isEmpty()) {
            return studentRepository.searchStudentsExceptCurrent(search, currentStudentId, pageable);
        } else {
            return studentRepository.findAllExceptCurrentStudent(currentStudentId, pageable);
        }
    }
    @Override
    public Page<Student> findAllExceptCurrentStudent(Long id, Pageable pageable) {
        return studentRepository.findAllExceptCurrentStudent(id, pageable);
    }

}

