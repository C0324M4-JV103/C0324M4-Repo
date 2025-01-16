package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.RegisterTeacherDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TeacherService;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class teacher_rgtController {
    private final TeacherService teacherService;
    private final TeamService teamService;
    private final UserService userService;
    private final StudentService studentService;

    @Autowired
    public teacher_rgtController(TeacherService teacherService, TeamService teamService, UserService userService, StudentService studentService) {
        this.teacherService = teacherService;
        this.teamService = teamService;
        this.userService = userService;
        this.studentService = studentService;
    }

    private Student getCurrentStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail);
        return studentService.findStudentByUserId(currentUser.getId());
    }

    @GetMapping("/listteachersrgt")
    public String listTeachers(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 10; // 10 giáo viên mỗi trang
        Pageable pageable = PageRequest.of(page, pageSize);

        // Lấy danh sách giáo viên
        Page<Teacher> teacherPage = teacherService.findAll(pageable);

        // Kiểm tra nếu teacherPage là null hoặc rỗng
        if (teacherPage == null || teacherPage.getContent().isEmpty()) {
            model.addAttribute("teachers", new ArrayList<>()); // Xử lý khi trang không có dữ liệu
            model.addAttribute("teacherTeamCount", new HashMap<Long, Integer>()); // Map đội rỗng
            model.addAttribute("totalPages", 0); // Không có trang nào
            model.addAttribute("pageNumber", 0); // Mặc định là trang đầu tiên
            model.addAttribute("errorMessage", "Không tìm thấy giáo viên.");
            return "student/register-teacher";
        }

        List<Teacher> teachers = teacherPage.getContent();
        Map<Long, Integer> teacherTeamCount = new HashMap<>();

        for (Teacher teacher : teachers) {
            int teamCount = teamService.countTeamsByTeacherId(teacher.getId());
            teacherTeamCount.put(teacher.getId(), teamCount);
        }

        model.addAttribute("teachers", teachers);
        model.addAttribute("teacherTeamCount", teacherTeamCount);
        model.addAttribute("totalPages", teacherPage.getTotalPages());
        model.addAttribute("pageNumber", page);

        return "student/register-teacher";
    }


    @PostMapping("/teachersrgt")
    public String teacherrgt(Long teamId, Long teacherId, RedirectAttributes redirectAttributes) {
        try {
            Student currentStudent = getCurrentStudent();

            // Kiểm tra xem sinh viên có đội hay không
            if (currentStudent.getTeam() == null) {
                redirectAttributes.addFlashAttribute("message", "Bạn cần có một nhóm để đăng ký giáo viên hướng dẫn.");
                redirectAttributes.addFlashAttribute("messageType", "error-message");
                return "redirect:/listteachersrgt";
            }

            // Kiểm tra xem sinh viên có phải là leader không
            if (!currentStudent.isLeader()) {
                redirectAttributes.addFlashAttribute("message", "Chỉ nhóm trưởng mới có thể đăng ký giáo viên hướng dẫn.");
                redirectAttributes.addFlashAttribute("messageType", "error-message");
                return "redirect:/listteachersrgt";
            }

            // Kiểm tra xem giáo viên đã đủ nhóm chưa
            int teamCount = teamService.countTeamsByTeacherId(teacherId);
            if (teamCount >= 5) {
                redirectAttributes.addFlashAttribute("message", "Giáo viên đã có đủ nhóm.");
                redirectAttributes.addFlashAttribute("messageType", "error-message");
                return "redirect:/listteachersrgt";
            }

            // Thực hiện đăng ký giáo viên
            Team team = teamService.teacherrgt(currentStudent.getTeam().getId(), teacherId);

        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/listteachersrgt";
        }
        return "redirect:/listteachersrgt";
    }

    @GetMapping("/teachersrgt/{id}")
    public ResponseEntity<RegisterTeacherDTO> getTeacherById(@PathVariable Long id) {
        Optional<Teacher> teacherOptional = teacherService.getTeacherById(id);
        if (teacherOptional.isPresent()) {
            Teacher teacher = teacherOptional.get();
            RegisterTeacherDTO teacherDTO = new RegisterTeacherDTO(
                    teacher.getId(),
                    teacher.getUser().getName(),
                    teacher.getUser().getEmail(),
                    teacher.getDegree(),
                    teacher.getUser().getPhoneNumber(),
                    teacher.getUser().getDob(),
                    teacher.getUser().getAddress(),
                    teacher.getUser().getGender(),
                    teacher.getUser().getAvatar(),
                    teacher.getFaculty() != null ? teacher.getFaculty().getName() : "Chưa có khoa" // Kiểm tra null
            );
            return ResponseEntity.ok(teacherDTO);
        }
        return ResponseEntity.notFound().build();
    }
}
