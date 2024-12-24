package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Clazz;
import com.c0324.casestudym5.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IStudentRepository extends JpaRepository<Student, Long> {
    @Query("select s from Student s join s.user u where" +
            "(:email is null or u.email like %:email%) and" +
            "(:name is null  or u.name like %:name%) and" +
            "(:classId is null or s.clazz.id = :classId)")
    Page<Student> getPageStudents(Pageable pageable,
                              @Param("email") String email,
                              @Param("name") String name,
                              @Param("classId") Long classId);

    @Query("select s from Student s join s.user u where" +
            "(:email is null or u.email like %:email%) and" +
            "(:name is null  or u.name like %:name%) and" +
            "(:classId is null or s.clazz.id = :classId)")
    List<Student> getStudents(@Param("email") String email,
                              @Param("name") String name,
                              @Param("classId") Long classId);
}
