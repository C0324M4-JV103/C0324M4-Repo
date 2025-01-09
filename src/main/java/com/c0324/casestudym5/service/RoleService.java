package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Role;
import com.c0324.casestudym5.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Role findByName(Role.RoleName name) {
        Optional<Role> role = Optional.ofNullable(roleRepository.findByName(name));
        return role.orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }
}
