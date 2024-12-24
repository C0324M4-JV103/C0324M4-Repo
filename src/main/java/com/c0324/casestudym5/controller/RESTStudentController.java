package com.c0324.casestudym5.controller;


import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.dto.StudentSearchDTO;
import com.c0324.casestudym5.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/admin/student")
public class RESTStudentController {
    @Autowired
    IStudentService studentService;

    @GetMapping
    public List<Student> getAllStudents(StudentSearchDTO search) {
        return studentService.getStudents(search);
    }
}
