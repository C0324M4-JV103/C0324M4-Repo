package com.c0324.casestudym5.controller;

<<<<<<< HEAD
import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.*;
import jakarta.validation.Valid;
=======
import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.service.TeacherService;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.service.UserService;
>>>>>>> 49b49008a9e04e47392a1a8936c36e54bf02fb8d
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

<<<<<<< HEAD
import java.util.HashSet;
import java.util.List;
=======
import java.security.Principal;
import java.util.Map;
>>>>>>> 49b49008a9e04e47392a1a8936c36e54bf02fb8d
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final RoleService roleService;
    private final TeacherService teacherService;
<<<<<<< HEAD
    private final FacultyService facultyService;
    private final UserService userService;
    private final MultiFileService multiFileService;

    @Autowired
    public TeacherController(TeacherService teacherService, FacultyService facultyService, UserService userService, RoleService roleService , MultiFileService multiFileService) {
        this.teacherService = teacherService;
        this.facultyService = facultyService;
        this.userService = userService;
        this.roleService = roleService;
        this.multiFileService = multiFileService;
=======
    private final TeamService teamService;
    private final UserService userService;

    @Autowired
    public TeacherController(TeacherService teacherService, TeamService teamService, UserService userService) {
        this.teacherService = teacherService;
        this.teamService = teamService;
        this.userService = userService;
>>>>>>> 49b49008a9e04e47392a1a8936c36e54bf02fb8d
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

<<<<<<< HEAD







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
=======
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
>>>>>>> 49b49008a9e04e47392a1a8936c36e54bf02fb8d
    }

}

