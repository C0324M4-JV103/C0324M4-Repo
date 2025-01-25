package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface TeacherService {
    Page<Teacher> getTeachersPage(int page, int size);

    Page<Teacher> searchTeachers(String email, String name, String phoneNumber, Pageable pageable);

    Optional<Teacher> getTeacherById(Long id);
    Teacher save(Teacher teacher);
    void createNewTeacher(TeacherDTO teacherDTO, MultipartFile avatar) throws Exception;
    void editTeacher(Long id, TeacherDTO teacherDTO, MultipartFile avatar) throws Exception;
    void deleteTeacherById(Long id) throws Exception;

    Page<Teacher> findAll(Pageable pageable);
    List<Teacher> getAllTeachers();

    Teacher getTeacherByEmail(String email);
}
