package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Invitation;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.service.InvitationService;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/team")
public class TeamController {

    private final StudentService studentService;
    private final UserService userService;
    private final TeamService teamService;
    private final InvitationService invitationService;

    @Autowired
    public TeamController(StudentService studentService, UserService userService, TeamService teamService, InvitationService invitationService) {
        this.studentService = studentService;
        this.userService = userService;
        this.teamService = teamService;
        this.invitationService = invitationService;
    }

    private Student getCurrentStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail);
        return studentService.findStudentByUserId(currentUser.getId());
    }

    @GetMapping("")
    public String formRegisterTeam(Model model) {

        if (!model.containsAttribute("team")) {
            model.addAttribute("team", new TeamDTO());
        }
        Student currentStudent = getCurrentStudent();
        Team currentTeam = currentStudent.getTeam();

        List<Student> availableStudents = studentService.findAllExceptCurrentStudent(currentStudent.getId());

        // nhận lời mời từ nhiều nhóm
        List<Invitation> invitation = invitationService.findByStudent(currentStudent);

        // check xem đang ở trong nhóm nào không
        boolean isInTeam = (currentTeam != null);

        model.addAttribute("list", availableStudents);
        model.addAttribute("isInTeam", isInTeam);
        model.addAttribute("invitation", invitation); // hiện thông tin lời mời
        return "team/form-team";
    }


    @PostMapping("/create")
    public String createTeam(@ModelAttribute("team") @Valid TeamDTO teamDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes)  {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.team", bindingResult);
            redirectAttributes.addFlashAttribute("team", teamDTO);
            return "redirect:/team";
        }
        if (teamService.existsByName(teamDTO.getName())) {
            bindingResult.rejectValue("name", "", "Tên nhóm đã tồn tại, vui lòng nhập tên khác!");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.team", bindingResult);
            redirectAttributes.addFlashAttribute("team", teamDTO);
            return "redirect:/team";
        }
        Student currentStudent = getCurrentStudent();

        List<Invitation> pendingInvitations = invitationService.findByStudent(currentStudent);
        if (!pendingInvitations.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn còn lời mời tham gia nhóm chưa xử lý!");
            return "redirect:/team";
        }

        Team newTeam = new Team();
        newTeam.setName(teamDTO.getName());
        newTeam.setStudents(List.of(currentStudent));

        teamService.save(newTeam);

        currentStudent.setTeam(newTeam);
        currentStudent.setLeader(true);
        studentService.save(currentStudent);

        redirectAttributes.addFlashAttribute("successMessages", "Nhóm đã được tạo thành công!");
        return "redirect:/team/info-team";
    }

    @PostMapping("/invite")
    public String inviteStudent(Long studentId, RedirectAttributes redirectAttributes) {

        Student invitedStudent = studentService.findById(studentId);

        // lấy tt người mời (team và inviter)
        Student currentStudent = getCurrentStudent();
        Team currentTeam = currentStudent.getTeam();

        if (currentTeam.getStudents().size() >= 5) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nhóm đã đủ 5 thành viên!");
            return "redirect:/team/info-team";
        }

        // nếu chưa vào nhóm nào và chưa được mời
        if (invitedStudent.getTeam() == null && !invitationService.existsByStudentAndTeam(invitedStudent, currentTeam)) {
            invitedStudent.setInvited(true);
            studentService.save(invitedStudent);

            // tạo lời mời và lưu riêng cho từng nhóm
            Invitation invitation = new Invitation();
            invitation.setStudent(invitedStudent);
            invitation.setTeam(currentTeam);
            invitation.setInviter(currentStudent);
            invitationService.save(invitation);

            redirectAttributes.addFlashAttribute("successMessage", "Lời mời đã được gửi thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Sinh viên này đã được mời!");
        }
        return "redirect:/team/info-team";
    }



    @PostMapping("/accept-invite")
    public String acceptInvite(@RequestParam Long invitationId, RedirectAttributes redirectAttributes) {

        Invitation invitation = invitationService.findById(invitationId);

        if (invitation != null) {
            Student student = invitation.getStudent();

            if (student != null) {
                student.setInvited(false);
                studentService.save(student);

                invitation.setAccepted(true);
                invitationService.save(invitation);

                student.setTeam(invitation.getTeam());
                studentService.save(student);

                invitationService.deleteAllByStudent(student); // xóa lời mời sau khi xác nhận
                redirectAttributes.addFlashAttribute("successMessage", "Bạn đã tham gia nhóm thành công!");
            }
        }
        return "redirect:/team/info-team";
    }



    @PostMapping("/reject-invite")
    public String rejectInvite(@RequestParam Long invitationId, RedirectAttributes redirectAttributes) {
        Invitation invitation = invitationService.findById(invitationId);

        if (invitation != null) {
            Student invitedStudent = invitation.getStudent();
            invitedStudent.setInvited(false);
            studentService.save(invitedStudent);
            invitationService.delete(invitation);
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã từ chối lời mời tham gia nhóm!");
        }
        return "redirect:/team";
    }


    @GetMapping("/info-team")
    public String teamInfo(Model model) {

        Student currentStudent = getCurrentStudent();

        Team team = currentStudent.getTeam();

        List<Student> availableStudents = studentService.findAllExceptCurrentStudent(currentStudent.getId());

        List<Invitation> invitation = null;
        if (currentStudent.isLeader()) {
            invitation = invitationService.findByStudent(currentStudent);
        }
        model.addAttribute("team", team);
        model.addAttribute("list", availableStudents);
        model.addAttribute("invitation", invitation);

        return "team/info-team";
    }

}