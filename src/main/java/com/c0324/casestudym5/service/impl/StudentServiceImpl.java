package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.StudentDTO;
import com.c0324.casestudym5.model.MultiFile;
import com.c0324.casestudym5.model.Role;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.*;
import com.c0324.casestudym5.service.FirebaseService;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final ClassRepository clazzRepository;
    private final MultiFileRepository multiFileRepository;
    private final RoleRepository roleRepository;
    private final FirebaseService firebaseService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository, ClassRepository classRepository, MultiFileRepository multiFileRepository, RoleRepository roleRepository, FirebaseService firebaseService, UserService userService, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {this.studentRepository = studentRepository;
        this.clazzRepository = classRepository;
        this.multiFileRepository = multiFileRepository;
        this.roleRepository = roleRepository;
        this.firebaseService = firebaseService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    public Student getStudentByUserEmail(String email) {
        return studentRepository.findByUserEmail(email);
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

    @Override
    public void createNewStudent(StudentDTO studentDTO, MultipartFile avatar) throws Exception {
        // Tạo đối tượng mới
        User newUser = new User();
        newUser.setName(studentDTO.getName());
        newUser.setEmail(studentDTO.getEmail());
        newUser.setDob(studentDTO.getDob());
        newUser.setGender(User.Gender.valueOf(studentDTO.getGender()));
        newUser.setPhoneNumber(studentDTO.getPhoneNumber());
        newUser.setAddress(studentDTO.getAddress());

        // Lấy link ảnh
        String urlImage = firebaseService.uploadFileToFireBase(avatar, "avatars");
        if (urlImage == null) {
            throw new Exception("Failed to upload avatar");
        }
        // Tạo một MultiFile và lưu vào DB
        MultiFile newAvatar = MultiFile.builder().url(urlImage).build();
        multiFileRepository.save(newAvatar);
        // Đặt ảnh đại diện cho User
        newUser.setAvatar(newAvatar);

        // Đặt role cho User
        Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT);
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        newUser.setRoles(roles);
        newUser.setPassword(passwordEncoder.encode("123"));
        userRepository.save(newUser);

        // Tạo Student mới
        Student newStudent = new Student();
        newStudent.setUser(newUser);
        newStudent.setClazz(clazzRepository.findById(studentDTO.getClazzId()).orElseThrow(() -> new RuntimeException("Lớp học không hợp lệ")));

        // Lưu Student
        studentRepository.save(newStudent);



    }

    @Override
    public List<Student> findStudentsByTeamId(Long teamId) {
        return studentRepository.findStudentsByTeamId(teamId);
    }


}
