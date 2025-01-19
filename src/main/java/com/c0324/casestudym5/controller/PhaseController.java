package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.CommentDTO;
import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.service.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PhaseController {

    private final TopicService topicService;
    private final CommentService commentService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public PhaseController(TopicService topicService, CommentService commentService, UserService userService, NotificationService notificationService) {
        this.topicService = topicService;
        this.commentService = commentService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addNotificationsToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail);
        if (currentUser != null) {
            List<NotificationDTO> notifications = notificationService.getTop3NotificationsByUserIdDesc(currentUser.getId());
            model.addAttribute("notifications", notifications);
        }
    }


    @GetMapping("/progress/{topicId}")
    public String showTopicProgress(@PathVariable Long topicId, Model model, Principal principal) {
        Topic topic = topicService.getTopicById(topicId);
        Team team = topic.getTeam();
        List<Student> students = team.getStudents();
        Set<Phase> phases = topic.getPhases();
        Set<Phase> sortedPhases = phases.stream()
                .sorted(Comparator.comparing(Phase::getPhaseNumber))
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
        model.addAttribute("phases", sortedPhases);
        model.addAttribute("comments", comments);
        model.addAttribute("curUserAvatar", curUserAvatar);
        return "phase";
    }

}
