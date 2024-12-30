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
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;


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
        // check xem có phải là nhóm trưởng không
        boolean isLeader = (currentTeam != null && currentStudent.isLeader());
        // check từng lời mời riêng biệt
        List<Long> invitedStudentIds = invitationService.findInvitedStudentIdsByTeam(currentTeam);
        for (Student student : availableStudents) {
            student.setInvited(invitedStudentIds.contains(student.getId()));
        }
        model.addAttribute("list", availableStudents);
        model.addAttribute("isInTeam", isInTeam);
        model.addAttribute("isLeader", isLeader);
        model.addAttribute("invitation", invitation); // hiện thông tin lời mời
        return "team/team-register";
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

        redirectAttributes.addFlashAttribute("successMessage", "Nhóm đã được tạo thành công!");
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
            return "redirect:/team";
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

            // Gửi email mời tham gia nhóm
            String subject = "Lời mời tham gia nhóm từ " + currentTeam.getName();
            String content = "Xin chào " + invitedStudent.getUser().getName() + ",\n\n"
                    + "Bạn đã được mời tham gia nhóm \"" + currentTeam.getName() + "\" bởi "
                    + currentStudent.getUser().getName() + ". Vui lòng kiểm tra thông tin trên hệ thống để chấp nhận lời mời.\n\n"
                    + "Bạn có thể xem và chấp nhận lời mời tại: "
                    + "<a href=\"http://localhost:8080/team" + "\">Xem lời mời ngay!</a>";

            invitationService.inviteStudent(studentId, subject, content);

            redirectAttributes.addFlashAttribute("successMessage", "Lời mời đã được gửi thành công!");
        }

        return "redirect:/team";
    }

    @PostMapping("/invitation/handle")
    public String handleInvitation(Long invitationId, boolean accept, RedirectAttributes redirectAttributes) {
        Invitation invitation = invitationService.findById(invitationId);

        if (accept) {
            Student student = invitation.getStudent();
            Team team = invitation.getTeam();

            if (team.getStudents().size() < 5) {
                student.setTeam(team);
                student.setInvited(false);
                studentService.save(student);
                // accept thì xóa all các lời mời khác
                invitationService.deleteAllByStudent(student);
                redirectAttributes.addFlashAttribute("successMessage", "Bạn đã tham gia nhóm thành công!");

                return "redirect:/team/info-team";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Nhóm đã đủ thành viên!");
            }
        } else {
            invitation.getStudent().setInvited(false);
            studentService.save(invitation.getStudent());
            invitationService.delete(invitation);

            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã từ chối lời mời!");
        }
        return "redirect:/team";
    }


    @GetMapping("/info-team")
    public String teamInfo(Model model) {
        Student currentStudent = getCurrentStudent();
        Team team = currentStudent.getTeam();
        List<Student> availableStudents = studentService.findAllExceptCurrentStudent(currentStudent.getId());
        // check xem có phải là nhóm trưởng không
        boolean isLeader = (team != null && currentStudent.isLeader());

        model.addAttribute("team", team);
        model.addAttribute("list", availableStudents);
        model.addAttribute("isLeader", isLeader);

        return "team/team-info";

    }


    // Task Huy
    @GetMapping("/management")
    public String showTeamPage(@RequestParam(name="name", defaultValue = "", required = false) String keyword,
                               @RequestParam(name="page", defaultValue = "0") int page,
                               Model model) {
        Page<TeamDTO> teamPage = teamService.getPageTeams(page, keyword);
        model.addAttribute("teams", teamPage.getContent());
        model.addAttribute("totalPages", teamPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        return "/admin/team-list";
    }

    @GetMapping("/delete/{teamId}")
    public String deleteTeam(@PathVariable("teamId") Long teamId) {
        teamService.deleteTeam(teamId);
        return "redirect:/admin/team";
    }

}





