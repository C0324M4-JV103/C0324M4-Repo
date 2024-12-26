package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.ChangePasswordDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.MultiFile;
import com.c0324.casestudym5.model.Role;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.RoleRepository;
import com.c0324.casestudym5.repository.UserRepository;
import com.c0324.casestudym5.service.FirebaseService;
import com.c0324.casestudym5.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FirebaseService firebaseService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder, FirebaseService firebaseService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.firebaseService = firebaseService;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(userName);
        if(user == null){
            throw new UsernameNotFoundException("Invalid email or password!");
        }
        Collection<SimpleGrantedAuthority> authorities = mapRolesToAuthorities(user.getRoles());

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(currentUserEmail);
    }

    @Override
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        User user = getCurrentUser();
        if(!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())){
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        save(user);
    }

    @Override
    @Transactional
    public void updateProfile(UserDTO userDTO) {
        if(findByEmail(userDTO.getEmail()) != null && !userDTO.getEmail().equals(getCurrentUser().getEmail())){
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        User currentUser = getCurrentUser();
        currentUser.setName(userDTO.getName());
        currentUser.setEmail(userDTO.getEmail());
        currentUser.setDob(userDTO.getDob());
        currentUser.setGender(User.Gender.valueOf(userDTO.getGender()));
        currentUser.setPhoneNumber(userDTO.getPhoneNumber());
        currentUser.setAddress(userDTO.getAddress());
        save(currentUser);
    }

    @Override
    public void changeAvatar(MultipartFile avatar) {
       User currentUser = getCurrentUser();
        if(!avatar.isEmpty()){
            try {
                MultiFile oldAvatar = currentUser.getAvatar();
                if(oldAvatar != null){
                    currentUser.setAvatar(null);
                    save(currentUser);
                    firebaseService.deleteFileFromFireBase(oldAvatar.getUrl());
                }
                String urlImage = firebaseService.uploadFileToFireBase(avatar);
                MultiFile newAvatar = MultiFile.builder().url(urlImage).build();
                currentUser.setAvatar(newAvatar);
                save(currentUser);
            } catch (Exception e){
                throw new IllegalArgumentException("Lỗi tải ảnh lên");
            }
        }
    }

    private Collection<SimpleGrantedAuthority> mapRolesToAuthorities(Collection<Role> roles){
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                .collect(Collectors.toList());
    }

    @PostConstruct
    public void init() {
        if (userRepository.findByEmail("admin@gmail.com") == null) {
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName(Role.RoleName.ROLE_ADMIN);
                roleRepository.save(adminRole);
            }

            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("123"));
            admin.setRoles(Collections.singleton(adminRole));
            admin.setPhoneNumber("123456789");
            admin.setName("Admin");
            admin.setAddress("Hanoi");
            admin.setDob(new Date());
            admin.setGender(User.Gender.MALE);
            save(admin);
        }
    }
}
