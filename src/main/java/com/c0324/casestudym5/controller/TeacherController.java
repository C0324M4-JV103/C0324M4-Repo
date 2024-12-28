package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.model.Faculty;
import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
=======
>>>>>>> c5c36bad34e37cd9c709601e710fa1400d70f1d8
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final TeacherService teacherService;

    @Autowired
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/detail/{id}")
    public String getTeacher(@PathVariable Long id, Model model) {
        Optional<Teacher> teacher = teacherService.getTeacherById(id);
        if (teacher.isPresent()) {
            model.addAttribute("teacher", teacher.get());
            return "admin/teacher/teacher-details";
        } else {
            return "common/404";
        }
    }

<<<<<<< HEAD
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("teacherDTO", new TeacherDTO());
        return "/admin/teacher/teacher-create";
    }

    @PostMapping("/create")
    public String createTeacher(@Valid @ModelAttribute("teacherDTO") TeacherDTO teacherDTO,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "/admin/teacher/teacher-create";
        }

        try {
            Teacher teacher = teacherService.createTeacher(teacherDTO);
            return "redirect:/admin/teacher/teacher-list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "/admin/teacher/teacher-create";
        }
    }

=======
>>>>>>> c5c36bad34e37cd9c709601e710fa1400d70f1d8
}

