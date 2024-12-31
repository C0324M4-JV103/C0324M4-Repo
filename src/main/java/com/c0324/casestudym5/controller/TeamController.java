package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/admin/team")
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

    @Autowired
    public TeamController(TeamService teamService, UserService userService) {
        this.teamService = teamService;
        this.userService = userService;
    }

    @GetMapping
    public String showTeamPage(@RequestParam(name="name", defaultValue = "", required = false) String keyword,
                               @RequestParam(name="page", defaultValue = "0") int page,
                               Model model) {
        Page<TeamDTO> teamPage = teamService.getPageTeams(page, keyword);
        model.addAttribute("teams", teamPage.getContent());
        model.addAttribute("totalPages", teamPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        return "/admin/team-list";
    }



    @MessageMapping("/delete-team")
    public String handleNotification(@Payload Map<String, Object> payload, Principal principal) {
        Long teamId = Long.parseLong(payload.get("teamId").toString());
        User sender = userService.findByEmail(principal.getName());
        teamService.deleteTeam(teamId, sender);
        return "redirect:/admin/team";
    }


}
