package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.service.TeacherService;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final TeacherService teacherService;
    private final TeamService teamService;
    private final UserService userService;

    @Autowired
    public TeacherController(TeacherService teacherService, TeamService teamService, UserService userService) {
        this.teacherService = teacherService;
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

}

