package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDetails loadUserByUsername(String userName);

    User findByEmail(String email);

    void save(User user);
}
