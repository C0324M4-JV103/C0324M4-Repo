package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.InvitedStudentDTO;
import com.c0324.casestudym5.dto.StudentDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import com.c0324.casestudym5.repository.*;
import com.c0324.casestudym5.service.FirebaseService;
import com.c0324.casestudym5.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
    private final TeacherRepository teacherRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository, ClassRepository classRepository, MultiFileRepository multiFileRepository, RoleRepository roleRepository, FirebaseService firebaseService, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, TeacherRepository teacherRepository) {this.studentRepository = studentRepository;
        this.clazzRepository = classRepository;
        this.multiFileRepository = multiFileRepository;
        this.roleRepository = roleRepository;
        this.firebaseService = firebaseService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.teacherRepository = teacherRepository;
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
    public InvitedStudentDTO getStudentDTOById(Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return null;
        }
        return new InvitedStudentDTO(
                student.getId(),
                student.getUser().getName(),
                student.getUser().getEmail(),
                student.getClazz(),
                student.getUser().getPhoneNumber(),
                student.getUser().getDob(),
                student.getUser().getAddress(),
                student.getUser().getGender(),
                student.getUser().getAvatar(),
                student.getTeamStatus()
        );
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
        newStudent.setCode(studentDTO.getCode());
        newStudent.setClazz(clazzRepository.findById(studentDTO.getClazzId()).orElseThrow(() -> new RuntimeException("Lớp học không hợp lệ")));

        // Lưu Student
        studentRepository.save(newStudent);
    }

    @Override
    public List<Student> findStudentsByTeamId(Long teamId) {
        return studentRepository.findStudentsByTeamId(teamId);
    }

    @Override
    public void saveAll(List<Student> students) {
        studentRepository.saveAll(students);
    }

    @Override
    public void editStudent(Long id, StudentDTO studentDTO, MultipartFile avatar) throws Exception {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (optionalStudent.isEmpty()) {
            throw new Exception("Teacher not found");
        }

        Student existingStudent = optionalStudent.get();

        existingStudent.setCode(studentDTO.getCode());

        User existingUser = existingStudent.getUser();

        existingUser.setName(studentDTO.getName());
        existingUser.setEmail(studentDTO.getEmail());
        existingUser.setDob(studentDTO.getDob());
        existingUser.setGender(User.Gender.valueOf(studentDTO.getGender()));
        existingUser.setPhoneNumber(studentDTO.getPhoneNumber());
        existingUser.setAddress(studentDTO.getAddress());

        if (avatar != null && !avatar.isEmpty()) {
            String urlImage = firebaseService.uploadFileToFireBase(avatar, "avatars");
            if (urlImage == null) {
                throw new Exception("Failed to upload avatar");
            }

            MultiFile newAvatar = MultiFile.builder().url(urlImage).build();
            multiFileRepository.save(newAvatar);
            existingUser.setAvatar(newAvatar);
        }else {
            existingUser.setAvatar(existingUser.getAvatar());
        }

        // Cập nhật lớp học
        Clazz newClazz = clazzRepository.findById(studentDTO.getClazzId()).orElseThrow(() -> new RuntimeException("Lớp học không hợp lệ"));
        existingStudent.setClazz(newClazz);

        userRepository.save(existingUser);
        studentRepository.save(existingStudent);
    }

    @Override
    public void deleteStudentById(Long id) throws Exception {
        Optional<Student> studentOptional = studentRepository.findById(id);
        if (studentOptional.isEmpty()) {
            throw new Exception("Không tìm thấy sinh viên với ID: " + id);
        }
        Student student = studentOptional.get();
        User user = student.getUser();
        studentRepository.deleteById(id);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Override
    public boolean existsByCode(String code) {
        return studentRepository.findByCode(code).isPresent();
    }

    @Override
    public Page<Student> findStudentsByTeacherId(Long userId, Pageable pageable, StudentSearchDTO search) {
        Teacher teacher = teacherRepository.findTeacherByUserId(userId);
        if (teacher == null) {
            return null;
        }
        return studentRepository.findStudentsByTeacherIdAndSearchCriteria(teacher.getId(), search.getName(), search.getEmail(), search.getClazzId(), pageable);
    }


}


