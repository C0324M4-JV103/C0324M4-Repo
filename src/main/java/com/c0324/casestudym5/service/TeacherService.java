package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Teacher;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface TeacherService {
    Page<Teacher> getTeachersPage(int page, int size);

    Page<Teacher> searchTeachers(String searchQuery, int page, int size);

    List<Teacher> getAllTeachers();
    Optional<Teacher> getTeacherById(Long id);
    Teacher save(Teacher teacher);
}
