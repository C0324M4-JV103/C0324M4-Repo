package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.ChangePasswordDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService extends UserDetailsService {

    UserDetails loadUserByUsername(String userName);

    User findByEmail(String email);

    void save(User user);

    User getCurrentUser();

    void changePassword(ChangePasswordDTO changePasswordDTO);

    void updateProfile(UserDTO user);

    void changeAvatar(MultipartFile avatar);

    List<User> fillAll();

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
