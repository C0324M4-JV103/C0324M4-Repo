package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private TopicService topicService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        List<Topic> latestTopics = topicService.getLatestTopics(3);
        model.addAttribute("topics", latestTopics);
        return "common/home-page";
    }

    @GetMapping("/user/notification")
    public String showNotification() {
        return "/common/notification";
    }

}
