package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.*;
import com.c0324.casestudym5.service.impl.ClazzService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final TeacherService teacherService;
    private final TeamService teamService;
    private final UserService userService;
    private final TopicService topicService;
    private final NotificationService notificationService;
    private final StudentService studentService;
    private final ClazzService classService;

    @Autowired
    public TeacherController(TeacherService teacherService, TeamService teamService, UserService userService, TopicService topicService, NotificationService notificationService, StudentService studentService, ClazzService classService) {
        this.teacherService = teacherService;
        this.teamService = teamService;
        this.userService = userService;
        this.topicService = topicService;
        this.notificationService = notificationService;
        this.studentService = studentService;
        this.classService = classService;
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

    @GetMapping("/detail/{id}")
    public String getTeacher(@PathVariable Long id, Model model) {
        Optional<Teacher> teacher = teacherService.getTeacherById(id);
        if (teacher.isPresent()) {
            model.addAttribute("teacher", teacher.get());
            return "admin/teacher/teacher-details";
        } else {
            return "common/404";
        }
    }

    @PostMapping("/change-avatar")
    public String showChangeAvatarForm(@RequestParam("avatar") MultipartFile avatar, Model model) {
        String fileName = avatar.getOriginalFilename();
        long fileSize = avatar.getSize();
        long maxFileSize = 5 * 1024 * 1024; // 5MB

        if (fileName != null && (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg"))) {
            if (fileSize <= maxFileSize) {
                userService.changeAvatar(avatar);
            } else {
                model.addAttribute("imageError", "Kích thước ảnh không được vượt quá 5MB");
            }
        } else {
            model.addAttribute("imageError", "Chỉ hỗ trợ ảnh có định dạng jpg, jpeg, png");
        }

        return "admin/teacher/teacher-create";
    }
    @GetMapping("/team")
    public String showTeamPage(@RequestParam(name="name", defaultValue = "", required = false) String keyword,
                               @RequestParam(name="page", defaultValue = "0") int page,
                               Model model, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        Page<TeamDTO> teamPage = teamService.getPageTeams(page, keyword, currentUser);
        model.addAttribute("teams", teamPage.getContent());
        model.addAttribute("totalPages", teamPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        return "/teacher/team-list";
    }

    @MessageMapping("/delete-team")
    public String handleNotification(@Payload Map<String, Object> payload, Principal principal) {
        Long teamId = Long.parseLong(payload.get("teamId").toString());
        User sender = userService.findByEmail(principal.getName());
        teamService.deleteTeam(teamId, sender);
        return "redirect:/teacher/team";
    }

    @MessageMapping("/set-deadline")
    public String setDeadline(@Payload Map<String, Object> payload, Principal principal) throws ParseException {
        Long teamId = Long.parseLong(payload.get("teamId").toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date newDeadline = sdf.parse(payload.get("deadline").toString());
        User setBy = userService.findByEmail(principal.getName());
        topicService.setNewDeadline(teamId, newDeadline, setBy);
        return "redirect:/teacher/team";
    }

    @GetMapping("/topics")
    public String getPendingTopics(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "6") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Topic> topicPage = topicService.getPendingTopicsPage(pageRequest);

        model.addAttribute("topics", topicPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", topicPage.getTotalPages());
        return "teacher/topic-approval";
    }

    @PostMapping("/topics/{id}/approve")
    public String approveTopic(@PathVariable Long id) {
        topicService.approveTopic(id);
        return "redirect:/teacher/topics";
    }

    @PostMapping("/topics/{id}/reject")
    public String rejectTopic(@PathVariable Long id) {
        topicService.rejectTopic(id);
        return "redirect:/teacher/topics";
    }

    @GetMapping("/student-list")
    public String getAllStudents(Model model,
                                 StudentSearchDTO search,
                                 @RequestParam(defaultValue = "0") int page, HttpSession session) {
        boolean isSearch = true;
        if (search.getName() != null && search.getName().isEmpty()) {
            search.setName(null);
        }
        if (search.getEmail() != null && search.getEmail().isEmpty()) {
            search.setEmail(null);
        }
        if (search.getClazzId() != null && search.getClazzId().toString().isEmpty()) {
            search.setClazzId(null);
        }
        if (search.getName() == null && search.getEmail() == null && search.getClazzId() == null) {
            isSearch = false;
        }
        User currentUser = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("pageTitle", "Danh sách sinh viên");
        Pageable pageable = PageRequest.of(page, 8);
        Page<Student> students = studentService.findStudentsByTeacherId(currentUser.getId(),pageable, search);
        model.addAttribute("students", students);
        model.addAttribute("classes", classService.getAllClazzes());
        model.addAttribute("search", search);
        model.addAttribute("isSearch", isSearch);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", students.getTotalPages());
        session.setAttribute("page", page);
        return "teacher/student-list";
    }

}
