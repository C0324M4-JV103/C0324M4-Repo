package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;

@Repository
=======

>>>>>>> c1eb9424a324ef6ccbd6409f068c1633a3fb777b
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
