package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.DocumentDTO;
import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final TeacherService teacherService;
    private final TeamService teamService;
    private final UserService userService;
    private final TopicService topicService;
    private final NotificationService notificationService;
    private final DocumentService documentService;
    private final FirebaseService firebaseService;

    @Autowired
    public TeacherController(TeacherService teacherService, TeamService teamService, UserService userService, TopicService topicService, NotificationService notificationService, DocumentService documentService, FirebaseService firebaseService) {
        this.teacherService = teacherService;
        this.teamService = teamService;
        this.userService = userService;
        this.topicService = topicService;
        this.notificationService = notificationService;
        this.documentService = documentService;
        this.firebaseService = firebaseService;
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
    public String showTeamPage(@RequestParam(name = "name", defaultValue = "", required = false) String keyword,
                               @RequestParam(name = "page", defaultValue = "0") int page,
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

    @GetMapping("/documents/upload")
    public String showDocumentsPage(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "5") int size,
                                    Model model) {
        Page<Document> documentPage = documentService.getDocumentsPage(page, size);
        model.addAttribute("documentDTO", new DocumentDTO());
        model.addAttribute("documents", documentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", documentPage.getTotalPages());
        model.addAttribute("totalItems", documentPage.getTotalElements());
        return "teacher/documents";
    }


    @PostMapping("/documents/upload")
    public String uploadDocument(@Valid @ModelAttribute("documentDTO") DocumentDTO documentDTO,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Vui lòng kiểm tra thông tin đã nhập!");
            return "teacher/documents";
        }

        Optional<Teacher> teacherOptional = teacherService.getTeacherById(documentDTO.getTeacher().getId());
        if (teacherOptional.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy giáo viên.");
            return "teacher/documents";
        }
        Teacher teacher = teacherOptional.get();

        String fileUrl = firebaseService.uploadFileToFireBase(documentDTO.getFileUrl(), "documents");
        if (fileUrl == null) {
            model.addAttribute("error", "Lỗi khi tải lên tài liệu.");
            return "teacher/documents";
        }

        Document document = new Document();
        document.setName(documentDTO.getName());
        document.setDescription(documentDTO.getDescription());
        document.setTeacher(teacher);
        document.setStatus(documentDTO.isStatus());

        documentService.saveDocument(document, fileUrl);

        model.addAttribute("success", "Tải tài liệu thành công!");
        return "redirect:/teacher/documents/upload";
    }
}

