package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.repository.TeacherRepository;
import com.c0324.casestudym5.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherServiceImpl implements TeacherService {
    private final TeacherRepository teacherRepository;

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public Page<Teacher> getTeachersPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return teacherRepository.findAll(pageable);
    }

    @Override
    public Page<Teacher> searchTeachers(String searchQuery, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            return teacherRepository.findByIdOrNameOrEmail(searchQuery, pageable);
        }
        return teacherRepository.findAll(pageable); // Nếu không có tìm kiếm, trả về tất cả
    }

    // Lấy tất cả giáo viên
    @Override
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    // Lấy thông tin giáo viên theo ID
    @Override
    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }
}
