package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final UserService userService;
    private final StudentService studentService;

    @Autowired
    public HomeController(UserService userService, StudentService studentService) {
        this.userService = userService;
        this.studentService = studentService;
    }

    private Student getCurrentStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail);
        return studentService.findStudentByUserId(currentUser.getId());
    }
    @GetMapping(value = {"/", "/home"})
    public String homePage(Model model) {
        Student currentStudent = getCurrentStudent();
        if (currentStudent == null) {
            return "redirect:/login";
        }

        Team team = currentStudent.getTeam();

        model.addAttribute("team", team);
        return "common/home-page";
    }

}
