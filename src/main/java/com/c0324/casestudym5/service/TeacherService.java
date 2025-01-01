package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.model.Faculty;
import com.c0324.casestudym5.model.Teacher;
<<<<<<< HEAD
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.FacultyRepository;
import com.c0324.casestudym5.repository.TeacherRepository;
import com.c0324.casestudym5.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
=======
>>>>>>> 49b49008a9e04e47392a1a8936c36e54bf02fb8d
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface TeacherService {
    Page<Teacher> getTeachersPage(int page, int size);

<<<<<<< HEAD
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


    public void save(Teacher teacher) {
        teacherRepository.save(teacher);
    }
=======
    Page<Teacher> searchTeachers(String searchQuery, int page, int size);

    List<Teacher> getAllTeachers();
    Optional<Teacher> getTeacherById(Long id);
>>>>>>> 49b49008a9e04e47392a1a8936c36e54bf02fb8d
}
