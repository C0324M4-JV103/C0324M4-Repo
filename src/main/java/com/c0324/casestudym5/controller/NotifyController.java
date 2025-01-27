package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class NotifyController {
    private final UserService userService;
    private final NotificationService notificationService;
    private final StudentService studentService;

    @Autowired
    public NotifyController(UserService userService, NotificationService notificationService, StudentService studentService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.studentService = studentService;
    }

    @ModelAttribute
    public void addNotificationsToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail);

        if (currentUser != null) {
            List<NotificationDTO> notifications = notificationService.getTop3NotificationsByUserIdDesc(currentUser.getId());
            int unreadCount = notificationService.countUnreadNotifications(currentUser.getId());

            Student currentStudent = studentService.findStudentByUserId(currentUser.getId());
            boolean isInTeam = currentStudent != null && currentStudent.getTeam() != null;

            model.addAttribute("isInTeam", isInTeam);
            model.addAttribute("notifications", notifications);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("newNotificationsCount", unreadCount);
            model.addAttribute("username", currentUser.getName());
        }
    }

}



