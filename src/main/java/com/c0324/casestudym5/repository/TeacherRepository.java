package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Teacher;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Teacher findById(long id);

    // Tìm kiếm giáo viên theo ID, tên hoặc email
    @Query("SELECT t FROM Teacher t JOIN t.user u WHERE " +
            "(?1 IS NULL OR CAST(t.id AS string) LIKE %?1%) OR " + // Tìm theo ID
            "(?1 IS NULL OR u.name LIKE %?1%) OR " +             // Tìm theo tên
            "(?1 IS NULL OR u.email LIKE %?1%)")                 // Tìm theo email
    Page<Teacher> findByIdOrNameOrEmail(String searchQuery, Pageable pageable);
}
=======
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

}
>>>>>>> c1eb9424a324ef6ccbd6409f068c1633a3fb777b
