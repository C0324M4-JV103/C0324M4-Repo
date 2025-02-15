package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.CommentDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.service.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PhaseController {

    private final TopicService topicService;
    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public PhaseController(TopicService topicService, CommentService commentService, UserService userService) {
        this.topicService = topicService;
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping("/progress/{topicId}")
    public String showTopicProgress(@PathVariable Long topicId, Model model, Principal principal) {
        Topic topic = topicService.getTopicById(topicId);
        Team team = topic.getTeam();
        List<Student> students = team.getStudents();
        Set<Phase> phases = topic.getPhases();

        // Filter to get the latest report for each phase
        Map<Integer, Phase> latestPhases = new HashMap<>();
        for (Phase phase : phases) {
            int phaseNumber = phase.getPhaseNumber();
            if (!latestPhases.containsKey(phaseNumber) ||
                    phase.getReportDate().isAfter(latestPhases.get(phaseNumber).getReportDate())) {
                latestPhases.put(phaseNumber, phase);
            }
        }

        // Sort reportedPhases by report date in descending order, handling null values
        List<Phase> sortedReportedPhases = phases.stream()
                .sorted(Comparator.comparing(Phase::getReportDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        List<CommentDTO> comments = commentService.getCommentsByTopicId(topicId);
        User currentUser = userService.findByEmail(principal.getName());

        boolean isStudentInTeam = students.stream().anyMatch(student -> student.getUser().getId().equals(currentUser.getId()));
        boolean isTeacherOfTeam = team.getTeacher().getUser().getId().equals(currentUser.getId());

        if (!isStudentInTeam && !isTeacherOfTeam) {
            return "common/404";
        }
        String curUserAvatar = currentUser.getAvatar().getUrl();

        model.addAttribute("team", team);
        model.addAttribute("topic", topic);
        model.addAttribute("students", students);
        model.addAttribute("phases", latestPhases.values());
        model.addAttribute("reportedPhases", sortedReportedPhases);
        model.addAttribute("comments", comments);
        model.addAttribute("curUserAvatar", curUserAvatar);
        return "phase";
    }
}
