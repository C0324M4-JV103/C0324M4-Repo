
package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Invitation;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;
    private final UserService userService;
    private final TeamService teamService;
    private final InvitationService invitationService;
    private final NotificationService notificationService;

    @Autowired
    public StudentController(StudentService studentService, UserService userService, TeamService teamService, InvitationService invitationService, NotificationService notificationService) {
        this.studentService = studentService;
        this.userService = userService;
        this.teamService = teamService;
        this.invitationService = invitationService;
        this.notificationService = notificationService;
    }


    @GetMapping("/{id}")
    public String view(@PathVariable("id") Long id, Model model, HttpSession httpSession) {
        Student student = studentService.getStudent(id);
        model.addAttribute("student", student);
        model.addAttribute("pageTitle", student.getUser().getName());
        model.addAttribute("page", httpSession.getAttribute("page"));
        return "admin/student/student-details";
    }

    private Student getCurrentStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail);
        return studentService.findStudentByUserId(currentUser.getId());
    }

    @GetMapping("/team")
    public String formRegisterTeam(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                   @RequestParam(name = "search", required = false, defaultValue = "") String search,
                                   Model model, Principal principal) {
        Student currentStudent = getCurrentStudent();
        Team currentTeam = currentStudent.getTeam();
        User currentUser = userService.findByEmail(principal.getName());
        List<NotificationDTO> notifications = notificationService.getTop3NotificationsByUserIdDesc(currentUser.getId());
        if (!model.containsAttribute("team")) {
            model.addAttribute("team", new TeamDTO());
        }
        Page<Student> availableStudents = studentService.getAvailableStudents(page, search, currentStudent.getId());
        List<Invitation> invitation = invitationService.findByStudent(currentStudent);
        boolean isInTeam = (currentTeam != null);
        boolean isLeader = (currentTeam != null && currentStudent.isLeader());

        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("isInTeam", isInTeam);
        model.addAttribute("isLeader", isLeader);
        model.addAttribute("invitation", invitation);
        model.addAttribute("list", availableStudents);// hiện thông tin lời mời
        model.addAttribute("currentTeam", currentTeam);
        model.addAttribute("notifications", notifications);
        model.addAttribute("invitationService", invitationService);
        model.addAttribute("totalPages", availableStudents.getTotalPages());
        return "team/team-register";
    }


    @PostMapping("/create-team")
    public String createTeam(@ModelAttribute("team") @Valid TeamDTO teamDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.team", bindingResult);
            redirectAttributes.addFlashAttribute("team", teamDTO);
            return "redirect:/student/team";
        }
        if (teamService.existsByName(teamDTO.getName())) {
            bindingResult.rejectValue("name", "", "Tên nhóm đã tồn tại, vui lòng nhập tên khác!");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.team", bindingResult);
            redirectAttributes.addFlashAttribute("team", teamDTO);
            return "redirect:/student/team";
        }
        Student currentStudent = getCurrentStudent();
        List<Invitation> pendingInvitations = invitationService.findByStudent(currentStudent);
        if (!pendingInvitations.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn còn lời mời tham gia nhóm chưa xử lý!");
            return "redirect:/student/team";
        }
        teamService.createNewTeam(teamDTO, currentStudent);
        redirectAttributes.addFlashAttribute("successMessage", "Nhóm đã được tạo thành công!");
        return "redirect:/student/info-team";
    }

    @PostMapping("/invite-team")
    public String inviteStudent(Long studentId, RedirectAttributes redirectAttributes) {
        Student currentStudent = getCurrentStudent();
        Team currentTeam = currentStudent.getTeam();
        if (currentTeam.getStudents().size() >= 5) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nhóm đã đủ 5 thành viên!");
            return "redirect:/student/team";
        }
        try {
            invitationService.inviteStudent(studentId, currentStudent, currentTeam);
            redirectAttributes.addFlashAttribute("successMessage", "Lời mời đã được gửi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi gửi lời mời: " + e.getMessage());
        }
        return "redirect:/student/team";
    }

    @PostMapping("/invitation/handle")
    public String handleInvitation(Long invitationId, boolean accept, RedirectAttributes redirectAttributes) {
        Invitation invitation = invitationService.findById(invitationId);
        Student student = invitation.getStudent();
        Team team = invitation.getTeam();
        if (accept) {
            if (team.getStudents().size() < 5) {
                student.setTeam(team);
                studentService.save(student);
                invitationService.deleteAllByStudent(student);
                redirectAttributes.addFlashAttribute("successMessage", "Bạn đã tham gia nhóm thành công!");
                return "redirect:/student/info-team";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Nhóm đã đủ thành viên!");
            }
        } else {
            invitationService.delete(invitation);
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã từ chối lời mời!");
        }
        return "redirect:/student/team";
    }



    @GetMapping("/info-team")
    public String teamInfo(Model model, Pageable pageable, Principal principal) {
        Student currentStudent = getCurrentStudent();
        Team team = currentStudent.getTeam();
        User currentUser = userService.findByEmail(principal.getName());
        List<NotificationDTO> notifications = notificationService.getTop3NotificationsByUserIdDesc(currentUser.getId());
        Page<Student> availableStudents = studentService.findAllExceptCurrentStudent(currentStudent.getId(), pageable);
        boolean isLeader = (team != null && currentStudent.isLeader());
        model.addAttribute("team", team);
        model.addAttribute("isLeader", isLeader);
        model.addAttribute("list", availableStudents);
        model.addAttribute("notifications", notifications);
        return "team/team-info";
    }
}
