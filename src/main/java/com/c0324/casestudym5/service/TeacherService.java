package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.repository.ITeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {
    @Autowired
    private ITeacherRepository iTeacherRepository;

    public List<Teacher> getAllTeachers() {
        return iTeacherRepository.findAll();
    }
}
