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

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;
    private final UserService userService;
    private final TeamService teamService;
    private final InvitationService invitationService;

    @Autowired
    public StudentController(StudentService studentService, UserService userService, TeamService teamService, InvitationService invitationService) {
        this.studentService = studentService;
        this.userService = userService;
        this.teamService = teamService;
        this.invitationService = invitationService;
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
                                   Model model) {

        if (!model.containsAttribute("team")) {
            model.addAttribute("team", new TeamDTO());
        }
        Student currentStudent = getCurrentStudent();
        Team currentTeam = currentStudent.getTeam();

        Pageable pageable = PageRequest.of(page - 1, 5);

        Page<Student> availableStudents;

        if (search != null && !search.isEmpty()) {
            availableStudents = studentService.searchStudentsExceptCurrent(search, currentStudent.getId(), pageable);
        } else {
            availableStudents = studentService.findAllExceptCurrentStudent(currentStudent.getId(), pageable);
        }
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

        Team newTeam = new Team();
        newTeam.setName(teamDTO.getName());
        newTeam.setStudents(List.of(currentStudent));

        teamService.save(newTeam);

        currentStudent.setTeam(newTeam);
        currentStudent.setLeader(true);
        studentService.save(currentStudent);

        redirectAttributes.addFlashAttribute("successMessage", "Nhóm đã được tạo thành công!");
        return "redirect:/student/info-team";
    }

    @PostMapping("/invite-team")
    public String inviteStudent(Long studentId, RedirectAttributes redirectAttributes) {

        Student invitedStudent = studentService.findById(studentId);

        Student currentStudent = getCurrentStudent();
        Team currentTeam = currentStudent.getTeam();

        if (currentTeam.getStudents().size() >= 5) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nhóm đã đủ 5 thành viên!");
            return "redirect:/student/team";
        }
        // nếu chưa vào nhóm nào và chưa được mời
        if (invitedStudent.getTeam() == null && !invitationService.existsByStudentAndTeam(invitedStudent, currentTeam)) {
            // tạo lời mời và lưu riêng cho từng nhóm
            Invitation invitation = new Invitation();
            invitation.setStudent(invitedStudent);
            invitation.setTeam(currentTeam);
            invitation.setInviter(currentStudent);
            invitationService.save(invitation);
            // send mail
            String subject = "Lời mời tham gia nhóm từ " + currentTeam.getName();
            String content = """
                    <html>
                    <body>
                        <p>Xin chào %s,</p>
                        <p>Bạn đã được mời tham gia nhóm <strong>"%s"</strong> bởi %s.</p>
                        <p>Vui lòng kiểm tra thông tin trên hệ thống để chấp nhận lời mời.</p>
                        <p>Bạn có thể xem và chấp nhận ngay bây giờ: <a href="http://localhost:8080/student/team">Xem lời mời ngay!</a></p>
                    </body>
                    </html>
                        """.formatted(invitedStudent.getUser().getName(), currentTeam.getName(), currentStudent.getUser().getName());
            invitationService.inviteStudent(studentId, subject, content);
            redirectAttributes.addFlashAttribute("successMessage", "Lời mời đã được gửi thành công!");
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
                // xóa khỏi db khi xác nhận
                invitationService.deleteAllByStudent(student);
                redirectAttributes.addFlashAttribute("successMessage", "Bạn đã tham gia nhóm thành công!");
                return "redirect:/student/info-team";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Nhóm đã đủ thành viên!");
            }
        } else {
            invitationService.delete(invitation);
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã từ chối lời mời!");
        }
        return "redirect:/student/team";
    }


    @GetMapping("/info-team")
    public String teamInfo(Model model, Pageable pageable) {

        Student currentStudent = getCurrentStudent();
        Team team = currentStudent.getTeam();

        Page<Student> availableStudents = studentService.findAllExceptCurrentStudent(currentStudent.getId(), pageable);

        boolean isLeader = (team != null && currentStudent.isLeader());

        model.addAttribute("team", team);
        model.addAttribute("list", availableStudents);
        model.addAttribute("isLeader", isLeader);

        return "team/team-info";

    }
}
