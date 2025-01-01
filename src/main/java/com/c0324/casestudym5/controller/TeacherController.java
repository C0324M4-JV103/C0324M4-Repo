package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final RoleService roleService;
    private final TeacherService teacherService;
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

}

