package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {
    private final UserService userService;
    private final NotificationService notificationService;
    private final TopicService topicService;

    @Autowired
    public HomeController(UserService userService, NotificationService notificationService, TopicService topicService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.topicService = topicService;
    }

    @GetMapping(value = {"/", "/home"})
    public String homePage(Model model, Principal principal) {
        List<Topic> latestTopics = topicService.getLatestTopics(3);
        model.addAttribute("topics", latestTopics);
        return "common/home-page";
    }

    @GetMapping("/mark-read")
    public ResponseEntity<?> markNotificationsAsRead(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User currentUser = userService.findByEmail(email);
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

}
