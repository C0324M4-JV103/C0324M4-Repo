package com.c0324.casestudym5.service;


import com.c0324.casestudym5.dto.InvitedStudentDTO;
import com.c0324.casestudym5.dto.StudentDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


public interface StudentService {
    Page<Student> getPageStudents(Pageable pageable, StudentSearchDTO search);
    List<Student> getStudents(StudentSearchDTO search);
    Student getStudent(Long id);
    Student getStudentByUserEmail(String email);
    void save(Student student);
    Student findById(Long id);
    InvitedStudentDTO getStudentDTOById(Long id);
    Student findStudentByUserId(Long id);
    String getStudentEmailById(Long id);
    Page<Student> getAvailableStudents(int page, String search, Long currentStudentId);
    Page<Student> findAllExceptCurrentStudent(Long currentStudentId, Pageable pageable);
    void createNewStudent(StudentDTO studentDTO, MultipartFile avatar) throws Exception;
    void editStudent(Long id, StudentDTO studentDTO, MultipartFile avatar) throws Exception;
    void deleteStudentById(Long id) throws Exception;
    List<Student> findStudentsByTeamId(Long teamId);
    void saveAll(List<Student> students);

    boolean existsByCode(String code);
    Page<Student> findStudentsByTeacherId(Long teacherId ,Pageable pageable, StudentSearchDTO search);
}
