package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.model.MultiFile;
import com.c0324.casestudym5.model.Role;
import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.MultiFileRepository;
import com.c0324.casestudym5.repository.RoleRepository;
import com.c0324.casestudym5.repository.TeacherRepository;
import com.c0324.casestudym5.repository.UserRepository;
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

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository, FirebaseService firebaseService,
                              MultiFileRepository multiFileRepository, RoleRepository roleRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.firebaseService = firebaseService;
        this.teacherRepository = teacherRepository;
        this.multiFileRepository = multiFileRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        newTeacher.setFaculty(newTeacher.getFaculty());
        teacherRepository.save(newTeacher);
    }

    @Override
    public void editTeacher(Long id, TeacherDTO teacherDTO, MultipartFile avatar) throws Exception {
        Optional<Teacher> optionalTeacher = teacherRepository.findById(id);
        if (!optionalTeacher.isPresent()) {
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

        existingTeacher.setDegree(teacherDTO.getDegree());
        userRepository.save(existingUser);
        teacherRepository.save(existingTeacher);
    }

    @Override
    public void deleteTeacherById(Long id) throws Exception {
        Optional<Teacher> teacherOptional = teacherRepository.findById(id);
        if (!teacherOptional.isPresent()) {
            throw new Exception("Không tìm thấy giáo viên với ID: " + id);
        }
        Teacher teacher = teacherOptional.get();
        User user = teacher.getUser();
        teacherRepository.deleteById(id);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}
