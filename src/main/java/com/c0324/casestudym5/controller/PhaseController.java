package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.CommentDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.PhaseService;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.service.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
public class PhaseController {

    private final PhaseService phaseService;
    private final TopicService topicService;
    private final StudentService studentService;
    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public PhaseController(PhaseService phaseService, TopicService topicService, StudentService studentService, CommentService commentService, UserService userService) {
        this.phaseService = phaseService;
        this.topicService = topicService;
        this.studentService = studentService;
        this.commentService = commentService;
        this.userService = userService;
    }


    @GetMapping("/progress/{topicId}")
    public String showTopicProgress(@PathVariable Long topicId, Model model, Principal principal) {
        Topic topic = topicService.getTopicById(topicId);
        Team team = topic.getTeam();
        List<Student> students = team.getStudents();
        Set<Phase> phases = topic.getPhases();
        List<CommentDTO> comments = commentService.getCommentsByTopicId(topicId);
        User currentUser = userService.findByEmail(principal.getName());
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
