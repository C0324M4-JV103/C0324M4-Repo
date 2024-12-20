package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.User;
<<<<<<< HEAD
import com.c0324.casestudym5.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
=======
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDetails loadUserByUsername(String userName);

    User findByEmail(String email);

    void save(User user);
>>>>>>> c1eb9424a324ef6ccbd6409f068c1633a3fb777b
}
