package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Teacher findById(long id);

    // Tìm kiếm giáo viên theo ID, tên hoặc email
    @Query("select s from Teacher s join s.user u where" +
            "(:email is null or u.email like %:email%) and" +
            "(:name is null or u.name like %:name%) and" +
            "(:phoneNumber is null or u.phoneNumber like %:phoneNumber%)")
    Page<Teacher> getPageTeacher(Pageable pageable,
                                 @Param("email") String email,
                                 @Param("name") String name,
                                 @Param("phoneNumber") String phoneNumber);

    Teacher findTeacherByUserEmail(String email);

    Teacher findTeacherByUserId(Long id);
}

