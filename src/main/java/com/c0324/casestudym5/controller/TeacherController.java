package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final TeacherService teacherService;
    private final TeamService teamService;
    private final UserService userService;
    private final TopicService topicService;

    @Autowired
    public TeacherController(TeacherService teacherService, TopicService topicService) {
    public TeacherController(TeacherService teacherService, TeamService teamService, UserService userService) {
        this.teacherService = teacherService;
        this.topicService = topicService;
        this.teamService = teamService;
        this.userService = userService;
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

    @GetMapping("/team")
    public String showTeamPage(@RequestParam(name="name", defaultValue = "", required = false) String keyword,
                               @RequestParam(name="page", defaultValue = "0") int page,
                               Model model, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        Page<TeamDTO> teamPage = teamService.getPageTeams(page, keyword, currentUser);
        model.addAttribute("teams", teamPage.getContent());
        model.addAttribute("totalPages", teamPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        return "/teacher/team-list";
    }

    @MessageMapping("/delete-team")
    public String handleNotification(@Payload Map<String, Object> payload, Principal principal) {
        Long teamId = Long.parseLong(payload.get("teamId").toString());
        User sender = userService.findByEmail(principal.getName());
        teamService.deleteTeam(teamId, sender);
        return "redirect:/teacher/team";
    }

    @GetMapping("/topics")
    public String getPendingTopics(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "6") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Topic> topicPage = topicService.getPendingTopicsPage(pageRequest);

        model.addAttribute("topics", topicPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", topicPage.getTotalPages());
        return "teacher/topic-approval";
    }

    @PostMapping("/topics/{id}/approve")
    public String approveTopic(@PathVariable Long id) {
        topicService.approveTopic(id);
        return "redirect:/teacher/topics";
    }

    @PostMapping("/topics/{id}/reject")
    public String rejectTopic(@PathVariable Long id) {
        topicService.rejectTopic(id);
        return "redirect:/teacher/topics";
    }
}

