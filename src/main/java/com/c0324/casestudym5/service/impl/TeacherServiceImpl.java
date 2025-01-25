package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.repository.*;
import com.c0324.casestudym5.service.FirebaseService;
import com.c0324.casestudym5.service.TeacherService;
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
public class TeacherServiceImpl implements TeacherService {
    private final TeacherRepository teacherRepository;
    private final FirebaseService firebaseService;
    private final MultiFileRepository multiFileRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FacultyRepository facultyRepository;

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository, FirebaseService firebaseService,
                              MultiFileRepository multiFileRepository, RoleRepository roleRepository,
                              UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, FacultyRepository facultyRepository) {
        this.firebaseService = firebaseService;
        this.teacherRepository = teacherRepository;
        this.multiFileRepository = multiFileRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.facultyRepository = facultyRepository;
    }

    @Override
    public Page<Teacher> getTeachersPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return teacherRepository.findAll(pageable);
    }

    @Override
    public Page<Teacher> searchTeachers(String email, String name, String phoneNumber, Pageable pageable) {
        if ((email == null || email.isEmpty()) &&
                (name == null || name.isEmpty()) &&
                phoneNumber == null) {
            return teacherRepository.findAll(pageable);
        }
        return teacherRepository.getPageTeacher(pageable, email, name, phoneNumber);
    }

    // Lấy thông tin giáo viên theo ID
    @Override
    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }

    @Override
    public Teacher save(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    @Override
    public void createNewTeacher(TeacherDTO teacherDTO, MultipartFile avatar) throws Exception {
        // Tạo đối tượng mới
        User newUser = new User();
        newUser.setName(teacherDTO.getName());
        newUser.setEmail(teacherDTO.getEmail());
        newUser.setDob(teacherDTO.getDob());
        newUser.setGender(User.Gender.valueOf(teacherDTO.getGender()));
        newUser.setPhoneNumber(teacherDTO.getPhoneNumber());
        newUser.setAddress(teacherDTO.getAddress());


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
        Role teacherRole = roleRepository.findByName(Role.RoleName.ROLE_TEACHER);
        Set<Role> roles = new HashSet<>();
        roles.add(teacherRole);
        newUser.setRoles(roles);
        newUser.setPassword(passwordEncoder.encode("123"));
        userRepository.save(newUser);

        Teacher newTeacher = new Teacher();
        newTeacher.setUser(newUser);
        newTeacher.setDegree(teacherDTO.getDegree());
        Faculty faculty = facultyRepository.findById(teacherDTO.getFacultyId())
                .orElseThrow(() -> new Exception("Faculty not found"));
        newTeacher.setFaculty(faculty);
        teacherRepository.save(newTeacher);
    }

    @Override
    public Page<Teacher> findAll(Pageable pageable) {
        return teacherRepository.findAll(pageable);
    }

    @Override
    public void editTeacher(Long id, TeacherDTO teacherDTO, MultipartFile avatar) throws Exception {
        Optional<Teacher> optionalTeacher = teacherRepository.findById(id);
        if (optionalTeacher.isEmpty()) {
            throw new Exception("Teacher not found");
        }

        Teacher existingTeacher = optionalTeacher.get();
        User existingUser = existingTeacher.getUser();

        existingUser.setName(teacherDTO.getName());
        existingUser.setEmail(teacherDTO.getEmail());
        existingUser.setDob(teacherDTO.getDob());
        existingUser.setGender(User.Gender.valueOf(teacherDTO.getGender()));
        existingUser.setPhoneNumber(teacherDTO.getPhoneNumber());
        existingUser.setAddress(teacherDTO.getAddress());

        if (avatar != null && !avatar.isEmpty()) {
            String urlImage = firebaseService.uploadFileToFireBase(avatar, "avatars");
            if (urlImage == null) {
                throw new Exception("Failed to upload avatar");
            }

            MultiFile newAvatar = MultiFile.builder().url(urlImage).build();
            multiFileRepository.save(newAvatar);
            existingUser.setAvatar(newAvatar);
        }
        Faculty faculty = facultyRepository.findById(teacherDTO.getFacultyId())
                .orElseThrow(() -> new Exception("Faculty not found"));
        existingTeacher.setFaculty(faculty);
        existingTeacher.setDegree(teacherDTO.getDegree());
        userRepository.save(existingUser);
        teacherRepository.save(existingTeacher);
    }

    @Override
    public void deleteTeacherById(Long id) throws Exception {
        Optional<Teacher> teacherOptional = teacherRepository.findById(id);
        if (teacherOptional.isEmpty()) {
            throw new Exception("Không tìm thấy giáo viên với ID: " + id);
        }
        Teacher teacher = teacherOptional.get();
        User user = teacher.getUser();
        teacherRepository.deleteById(id);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @Override
    public Teacher getTeacherByEmail(String email) {
        return teacherRepository.findTeacherByUserEmail(email);
    }
}
