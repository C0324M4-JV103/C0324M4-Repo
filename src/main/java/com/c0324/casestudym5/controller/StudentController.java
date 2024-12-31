package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.RegisterTopicDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final TopicService topicService;
    private final TeamService teamService;

    @Autowired
    public StudentController(StudentService studentService, TeamService teamService, TopicService topicService) {
        this.studentService = studentService;
        this.topicService = topicService;
        this.teamService = teamService;
    }

    @GetMapping("/team")
    public String showTeam(Model model, Principal principal) {
        Student student = studentService.getStudentByUserEmail(principal.getName());
        model.addAttribute("team", teamService.getTeamByStudentId(1L));
        model.addAttribute("student", student);
        return "student/team";
    }

    @GetMapping("/register-topic")
    public String showRegistTopicForm(Model model) {
        model.addAttribute("registerTopic", new RegisterTopicDTO());
        return "student/register-topic";
    }

    @PostMapping("/register-topic")
    public String registTopic(@ModelAttribute RegisterTopicDTO registerTopicDTO, Principal principal) {
        topicService.registerTopic(registerTopicDTO, principal.getName());
        return "redirect:/student/team";
    }

}
