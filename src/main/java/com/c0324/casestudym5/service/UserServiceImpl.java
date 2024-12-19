package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Role;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.RoleRepository;
import com.c0324.casestudym5.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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
