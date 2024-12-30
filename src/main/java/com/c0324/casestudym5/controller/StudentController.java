package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.RegistTeamDTO;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final TeamService teamService;

    @Autowired
    public StudentController(StudentService studentService, TeamService teamService) {
        this.studentService = studentService;
        this.teamService = teamService;
    }

    @GetMapping("/regist-topic")
    public String showRegistTopicForm(Model model) {
        model.addAttribute("registTeamDTO", new RegistTeamDTO());
        return "student/regist-topic";
    }

    @PostMapping("/regist-topic")
    public String registTopic(
            @RequestParam("teamId") Long teamId,
            @RequestParam("topicId") Long topicId) {
        // get studentId from session

        teamService.registTopic(teamId, topicId);
        return "redirect:/admin/team";
    }

}
