package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.dto.ProgressReportDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class TopicController {

    private final TopicService topicService;
    private final NotificationService notificationService;
    private final StudentService studentService;
    private final UserService userService;

    @Autowired
    public TopicController(TopicService topicService, NotificationService notificationService, StudentService studentService, UserService userService) {
        this.topicService = topicService;
        this.notificationService = notificationService;
        this.studentService = studentService;
        this.userService = userService;
    }

    @GetMapping("/topics")
    public String getTopics(@RequestParam(defaultValue = "0") int page, Model model) {
        PageRequest pageRequest = PageRequest.of(page, 12, Sort.by("id").descending());
        Page<Topic> topics = topicService.getAllTopics(pageRequest);
        model.addAttribute("topics", topics);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", topics.getTotalPages());
        return "student/topic-list";
    }

    @GetMapping("/topics/{id}")
    public String getTopicDetail(@PathVariable Long id, Model model) {
        Topic topic = topicService.getTopicById(id);
        model.addAttribute("topic", topic);
        return "student/topic-detail";
    }

    @GetMapping("/topics/{topicId}/progress/{phaseNum}")
    public String showProgressReportForm(@PathVariable Long topicId, @PathVariable Integer phaseNum ,Model model) {
        Topic topic = topicService.getTopicById(topicId);
        User currentUser = getCurrentUser();
        Student student = studentService.findStudentByUserId(currentUser.getId());
        Team userTeam = student.getTeam();
        if (topic == null || topic.getApproved() != AppConstants.APPROVED || !userTeam.getTopic().getId().equals(topic.getId())) {
            return "common/404";
        }
        List<NotificationDTO> notifications = notificationService.getTop3NotificationsByUserIdDesc(currentUser.getId());
        model.addAttribute("notifications", notifications);
        model.addAttribute("topic", topic);
        model.addAttribute("phaseNumber", phaseNum);
        model.addAttribute("reportTopic", new ProgressReportDTO());
        return "team/progress-report";
    }

    @PostMapping("/topics/handle-progress-report/{id}")
    public String submitProgressReport(@PathVariable Long id, @ModelAttribute ProgressReportDTO progressReportDTO) {
        Topic topic = topicService.getTopicById(id);
        User currentUser = getCurrentUser();
        Student student = studentService.findStudentByUserId(currentUser.getId());
        Team userTeam = student.getTeam();
        if (topic == null || topic.getApproved() != AppConstants.APPROVED || !userTeam.getTopic().getId().equals(topic.getId())) {
            return "common/404";
        }
//        topicService.submitProgressReport(topic, progressReportDTO);
        return "redirect:/topics/progress/" + id;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userService.findByEmail(userEmail);
    }
}
