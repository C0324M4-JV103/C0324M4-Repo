package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        User currentUser = userService.findByEmail(principal.getName());

        List<NotificationDTO> notifications = notificationService.getTop3NotificationsByUserIdDesc(currentUser.getId());
        List<Topic> latestTopics = topicService.getLatestTopics(3);

        model.addAttribute("topics", latestTopics);
        model.addAttribute("notifications", notifications);

        return "common/home-page";
    }


}
