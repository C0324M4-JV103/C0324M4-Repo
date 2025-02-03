package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final BlogsService blogsService;

    private final TeacherService teacherService;
    @Autowired
    public HomeController(UserService userService, NotificationService notificationService,
                          TopicService topicService, BlogsService blogsService, TeacherService teacherService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.topicService = topicService;
        this.blogsService = blogsService;
        this.teacherService = teacherService;
    }

    @GetMapping(value = {"/", "/home"})
    public String homePage(Model model) {
        Page<Teacher> list = teacherService.getTeachersPage(1,4);
        List<Topic> latestTopics = topicService.getLatestTopics(3);
        List<Blogs> latestBlogs = blogsService.getLatestBlogs(5);

        model.addAttribute("topics", latestTopics);
        model.addAttribute("blogs", latestBlogs);
        model.addAttribute("list", list);

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
