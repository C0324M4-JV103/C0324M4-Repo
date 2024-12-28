package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.model.Faculty;
import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.FacultyRepository;
import com.c0324.casestudym5.repository.TeacherRepository;
import com.c0324.casestudym5.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;

    @Autowired
    public TeacherService(TeacherRepository teacherRepository, FacultyRepository facultyRepository, UserRepository userRepository, FacultyRepository facultyRepository1, UserRepository userRepository1){
        this.teacherRepository = teacherRepository;
        this.facultyRepository = facultyRepository1;
        this.userRepository = userRepository1;
    }

    public Page<Teacher> getTeachersPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return teacherRepository.findAll(pageable);
    }

    // Tìm kiếm giáo viên theo ID, tên hoặc email
    public Page<Teacher> searchTeachers(String searchQuery, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            return teacherRepository.findByIdOrNameOrEmail(searchQuery, pageable);
        }
        return teacherRepository.findAll(pageable); // Nếu không có tìm kiếm, trả về tất cả
    }

    // Lấy tất cả giáo viên
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    // Lấy thông tin giáo viên theo ID
    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }

    public Teacher createTeacher(TeacherDTO teacherDTO) {

        Faculty faculty = facultyRepository.findById(teacherDTO.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Khoa không tồn tại"));

        User user = userRepository.findById(teacherDTO.getUserDTO().getId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        Teacher.Degree degree;
        try {
            degree = Teacher.Degree.valueOf(teacherDTO.getDegree());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Học vị không hợp lệ");
        }

        Teacher teacher = new Teacher();
        teacher.setFaculty(faculty);
        teacher.setUser(user);
        teacher.setDegree(degree);

        return teacherRepository.save(teacher);
    }

}
