package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.RegisterTopicDTO;
import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;
    private final UserService userService;
    private final TeamService teamService;
    private final TopicService topicService;
    private final InvitationService invitationService;

    @Autowired
    public StudentController(StudentService studentService, UserService userService, TeamService teamService, TopicService topicService, InvitationService invitationService) {
        this.studentService = studentService;
        this.userService = userService;
        this.teamService = teamService;
        this.topicService = topicService;
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

    @GetMapping("/team2")
    public String showTeam(Model model, Principal principal) {
        Student student = studentService.getStudentByUserEmail(principal.getName());
        model.addAttribute("team", teamService.getTeamByStudentId(student.getId()));
        model.addAttribute("student", student);
        return "student/team";
    }


    @PostMapping("/create-team")
    public String createTeam(@ModelAttribute("team") @Valid TeamDTO teamDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.Bi" +
                    "ndingResult.team", bindingResult);
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
        if (invitedStudent.getTeam() == null && !invitationService.existsByStudentAndTeam(invitedStudent, currentTeam)) {
            invitationService.saveInvitation(invitedStudent, currentStudent, currentTeam);
            invitationService.inviteStudent(studentId, currentStudent, currentTeam); // send mail
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

        model.addAttribute("team", team);
        model.addAttribute("list", availableStudents);
        model.addAttribute("student", currentStudent);

        return "team/team-info";

    }

    @GetMapping("/register-topic")
    public String showRegisterTopicForm(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        if (!model.containsAttribute("registerTopic")) {
            model.addAttribute("registerTopic", new RegisterTopicDTO());
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Topic> topicPage = topicService.findByStatus(1, pageable);
        model.addAttribute("topics", topicPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", topicPage.getTotalPages());
        return "student/register-topic";
    }

    @PostMapping("/handle-register-topic")
    public String registerTopic(@Valid @ModelAttribute RegisterTopicDTO registerTopicDTO, BindingResult result, Principal principal, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerTopic", result);
            redirectAttributes.addFlashAttribute("registerTopic", registerTopicDTO);
            return "redirect:/student/register-topic";
        }

        MultipartFile image = registerTopicDTO.getImage();
        MultipartFile description = registerTopicDTO.getDescription();
        long maxFileSizeImage = 5 * 1024 * 1024; // 5MB
        long maxFileSizeDescription = 15 * 1024 * 1024; // 15MB

        // Check size and format of image
        if (image != null && !image.isEmpty()) {
            String imageName = image.getOriginalFilename();
            long imageSize = image.getSize();
            if (imageName != null && (imageName.endsWith(".jpg") || imageName.endsWith(".png") || imageName.endsWith(".jpeg"))) {
                if (imageSize > maxFileSizeImage) {
                    redirectAttributes.addFlashAttribute("imageError", "Kích thước ảnh không được vượt quá 5MB");
                    redirectAttributes.addFlashAttribute("registerTopic", registerTopicDTO);
                    return "redirect:/student/register-topic";
                }
            } else {
                redirectAttributes.addFlashAttribute("imageError", "Chỉ hỗ trợ ảnh có định dạng jpg, jpeg, png");
                redirectAttributes.addFlashAttribute("registerTopic", registerTopicDTO);
                return "redirect:/student/register-topic";
            }
        }

        // Check size and format of description
        if (description != null && !description.isEmpty()) {
            String descriptionName = description.getOriginalFilename();
            long descriptionSize = description.getSize();
            if (descriptionName != null && (descriptionName.endsWith(".xls") || descriptionName.endsWith(".xlsx") || descriptionName.endsWith(".doc") || descriptionName.endsWith(".docx") || descriptionName.endsWith(".ppt") || descriptionName.endsWith(".pptx"))) {
                if (descriptionSize > maxFileSizeDescription) {
                    redirectAttributes.addFlashAttribute("descriptionError", "Kích thước tệp mô tả không được vượt quá 15MB");
                    redirectAttributes.addFlashAttribute("registerTopic", registerTopicDTO);
                    return "redirect:/student/register-topic";
                }
            } else {
                redirectAttributes.addFlashAttribute("descriptionError", "Chỉ hỗ trợ tệp có định dạng xls, xlsx, doc, docx, ppt, pptx");
                redirectAttributes.addFlashAttribute("registerTopic", registerTopicDTO);
                return "redirect:/student/register-topic";
            }
        }

        boolean isRegistered = topicService.registerTopic(registerTopicDTO, principal.getName());
        if (!isRegistered) {
            redirectAttributes.addFlashAttribute("registerError", "Đăng ký đề tài thất bại");
            redirectAttributes.addFlashAttribute("registerTopic", registerTopicDTO);
            return "redirect:/student/register-topic";
        }
        redirectAttributes.addFlashAttribute("registerSuccess", "Đăng ký đề tài thành công");
        return "redirect:/student/info-team";
    }

}
