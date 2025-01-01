package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.RegisterTopicDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
        model.addAttribute("team", teamService.getTeamByStudentId(student.getId()));
        model.addAttribute("student", student);
        return "student/team";
    }

    @GetMapping("/register-topic")
    public String showRegisterTopicForm(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Topic> topicPage = topicService.getAllTopics(pageable);
        model.addAttribute("registerTopic", new RegisterTopicDTO());
        model.addAttribute("topics", topicPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", topicPage.getTotalPages());
        return "student/register-topic";
    }

    @PostMapping("/register-topic")
    public String registerTopic(@ModelAttribute RegisterTopicDTO registerTopicDTO, Principal principal) {
        topicService.registerTopic(registerTopicDTO, principal.getName());
        return "redirect:/student/team";
    }

}
