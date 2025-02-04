package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
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

    Student findByUserEmail(String email);
    Optional<Student> findByCode(String code);

    Student findStudentByUserId(Long id);
    @Query("select s from Student s where s.id != :currentStudentId " +
            "order by case when s.team is null then 0 else 1 end, SUBSTRING_INDEX(s.user.name, ' ', -1) asc ")
    Page<Student> findAllExceptCurrentStudent(Long currentStudentId, Pageable pageable);
    @Query("select s from Student s where s.id != :currentStudentId " +
            "and (s.user.name like %:search% or s.code like %:search%)")
    Page<Student> searchStudentsExceptCurrent(@Param("search") String search,
                                              @Param("currentStudentId") Long currentStudentId, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE s.team.id = :teamId")
    List<Student> findStudentsByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT s FROM Student s WHERE s.team.teacher.id = :teacherId " +
            "AND (:name IS NULL OR s.user.name LIKE %:name%) " +
            "AND (:email IS NULL OR s.user.email LIKE %:email%) " +
            "AND (:clazzId IS NULL OR s.clazz.id = :clazzId)")
    Page<Student> findStudentsByTeacherIdAndSearchCriteria(@Param("teacherId") Long teacherId,
                                                           @Param("name") String name,
                                                           @Param("email") String email,
                                                           @Param("clazzId") Long clazzId,
                                                           Pageable pageable);
}
