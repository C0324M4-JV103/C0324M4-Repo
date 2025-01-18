package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.CommentDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.*;
import com.c0324.casestudym5.service.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@Controller
public class PhaseController {

    private final PhaseService phaseService;
    private final TopicService topicService;
    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public PhaseController(PhaseService phaseService, TopicService topicService, CommentService commentService, UserService userService) {
        this.phaseService = phaseService;
        this.topicService = topicService;
        this.commentService = commentService;
        this.userService = userService;
    }


    @GetMapping("/progress/{topicId}")
    public String showTopicProgress(@PathVariable Long topicId, Model model, Principal principal) {
        Topic topic = topicService.getTopicById(topicId);
        Team team = topic.getTeam();
        List<Student> students = team.getStudents();
        User currentUser = userService.findByEmail(principal.getName());

        boolean isStudentInTeam = students.stream().anyMatch(student -> student.getUser().getId().equals(currentUser.getId()));
        boolean isTeacherOfTeam = team.getTeacher().getUser().getId().equals(currentUser.getId());

        if (!isStudentInTeam && !isTeacherOfTeam) {
            return "common/404";
        }

        List<Phase> phases = phaseService.findPhasesByTopic(topic);
        List<CommentDTO> comments = commentService.getCommentsByTopicId(topicId);
        String curUserAvatar = currentUser.getAvatar().getUrl();

        model.addAttribute("team", team);
        model.addAttribute("topic", topic);
        model.addAttribute("students", students);
        model.addAttribute("phases", phases);
        model.addAttribute("comments", comments);
        model.addAttribute("curUserAvatar", curUserAvatar);
        return "phase";
    }

}
