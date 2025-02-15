package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.*;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.*;
import com.c0324.casestudym5.util.AppConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;
    private final UserService userService;
    private final TeamService teamService;
    private final TopicService topicService;
    private final InvitationService invitationService;
    private final TeacherService teacherService;
    private final DocumentService documentService;

    @Autowired
    public StudentController(StudentService studentService, UserService userService, TeamService teamService, TopicService topicService, InvitationService invitationService, TeacherService teacherService, DocumentService documentService) {
        this.studentService = studentService;
        this.userService = userService;
        this.teamService = teamService;
        this.topicService = topicService;
        this.invitationService = invitationService;
        this.teacherService = teacherService;
        this.documentService = documentService;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
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


    @GetMapping("/menu")
    public String studentMenu() {
        Student currentStudent = getCurrentStudent();
        if (currentStudent.getTeam() != null) {
            return "redirect:/student/info-team";
        }
        return "redirect:/student/team";
    }

    @GetMapping("/student-details/{id}")
    @ResponseBody
    public ResponseEntity<InvitedStudentDTO> getStudentById(@PathVariable Long id) {
        InvitedStudentDTO studentDTO = studentService.getStudentDTOById(id);
        if (studentDTO != null) {
            return ResponseEntity.ok(studentDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/team")
    public String formRegisterTeam(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                   @RequestParam(name = "search", required = false, defaultValue = "") String search,
                                   Model model) {
        Student currentStudent = getCurrentStudent();
        Team currentTeam = currentStudent.getTeam();

        if (!model.containsAttribute("team")) {
            model.addAttribute("team", new TeamDTO());
        }

        if (currentTeam != null && !currentStudent.isLeader()) {
            return "common/404";
        }


        Page<Student> availableStudents = studentService.getAvailableStudents(page, search, currentStudent.getId());
        List<Invitation> invitation = invitationService.findByStudent(currentStudent);

        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("invitation", invitation); // hiện thông tin lời mời
        model.addAttribute("list", availableStudents);
        model.addAttribute("currentTeam", currentTeam);
        model.addAttribute("currentStudent", currentStudent);
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
        String result = invitationService.handleInvitation(invitationId, accept);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã tham gia nhóm thành công!");
            return "redirect:/student/info-team";
        } else if ("full".equals(result)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nhóm đã đủ thành viên!");
            redirectAttributes.addFlashAttribute("errorType", "full");
        } else if ("declined".equals(result)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã từ chối lời mời!");
            redirectAttributes.addFlashAttribute("errorType", "declined");
        }
        return "redirect:/student/team";
    }


    @GetMapping("/info-team")
    public String teamInfo(Model model, Pageable pageable) {
        Student currentStudent = getCurrentStudent();
        Team team = currentStudent.getTeam();
        if (team == null) {
            return "common/404";
        }
        Page<Student> availableStudents = studentService.findAllExceptCurrentStudent(currentStudent.getId(), pageable);

        model.addAttribute("team", team);
        model.addAttribute("student", currentStudent);
        model.addAttribute("list", availableStudents);
        return "team/team-info";

    }

    @GetMapping("/register-topic")
    public String showRegisterTopicForm(Model model,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        RedirectAttributes redirectAttributes) {
        Student currentStudent = getCurrentStudent();
        Team currentTeam = currentStudent.getTeam();
        if (currentTeam == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa có nhóm");
            return "redirect:/student/info-team";
        }
        if (currentTeam.getTopic() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nhóm đã có đề tài hoặc đang đợi phê duyệt");
            return "redirect:/student/info-team";
        }
        if (currentTeam.getTeacher() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng ký giáo viên trước khi đăng ký đề tài!");
            return "redirect:/student/info-team";
        }
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
                    redirectAttributes.addFlashAttribute("errorMessage", "Kích thước ảnh không được vượt quá 5MB");
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
                    redirectAttributes.addFlashAttribute("errorMessage", "Kích thước tệp mô tả không được vượt quá 15MB");
                    redirectAttributes.addFlashAttribute("registerTopic", registerTopicDTO);
                    return "redirect:/student/register-topic";
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Chỉ hỗ trợ tệp có định dạng xls, xlsx, doc, docx, ppt, pptx");
                redirectAttributes.addFlashAttribute("registerTopic", registerTopicDTO);
                return "redirect:/student/register-topic";
            }
        }

        boolean isRegistered = topicService.registerTopic(registerTopicDTO, principal.getName());
        if (!isRegistered) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đăng ký đề tài thất bại");
            redirectAttributes.addFlashAttribute("registerTopic", registerTopicDTO);
            return "redirect:/student/register-topic";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký đề tài thành công");
        return "redirect:/student/info-team";
    }

    @GetMapping("/progress")
    public String redirectToProgress() {
        Student currentStudent = getCurrentStudent();
        Team currentTeam = currentStudent.getTeam();
        if (currentTeam == null) {
            return "common/404";
        }
        if (currentTeam.getTopic() != null && currentTeam.getTopic().getApproved() == AppConstants.TOPIC_APPROVED) {
            Long topicId = currentTeam.getTopic().getId();
            return "redirect:/progress/" + topicId;
        }
        return "redirect:/student/info-team";
    }


    @GetMapping("/list-teacher")
    public String getAllTeachers(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 5; // 10 giáo viên mỗi trang
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

        List<Long> registeredTeacherIds = new ArrayList<>();
        Student currentStudent = getCurrentStudent();
        if (currentStudent.getTeam() != null && currentStudent.getTeam().getTeacher() != null) {
            registeredTeacherIds.add(currentStudent.getTeam().getTeacher().getId());
        }

        model.addAttribute("teachers", teachers);
        model.addAttribute("teacherTeamCount", teacherTeamCount);
        model.addAttribute("totalPages", teacherPage.getTotalPages());
        model.addAttribute("pageNumber", page);
        model.addAttribute("registeredTeacherIds", registeredTeacherIds);

        return "student/register-teacher";
    }

    @PostMapping("/register-teacher")
    public String registerTeacherForTopic(Long teacherId, RedirectAttributes redirectAttributes) {
        try {
            Student currentStudent = getCurrentStudent();

            // Kiểm tra xem sinh viên có đội hay không
            if (currentStudent.getTeam() == null) {
                redirectAttributes.addFlashAttribute("message", "Bạn cần có một nhóm để đăng ký giáo viên hướng dẫn.");
                redirectAttributes.addFlashAttribute("messageType", "error-message");
                return "redirect:/student/list-teacher";
            }

            // Kiểm tra xem sinh viên có phải là leader không
            if (!currentStudent.isLeader()) {
                redirectAttributes.addFlashAttribute("message", "Chỉ nhóm trưởng mới có thể đăng ký giáo viên hướng dẫn.");
                redirectAttributes.addFlashAttribute("messageType", "error-message");
                return "redirect:/student/list-teacher";
            }

            // Kiểm tra xem giáo viên đã đủ nhóm chưa
            int teamCount = teamService.countTeamsByTeacherId(teacherId);
            if (teamCount >= 5) {
                redirectAttributes.addFlashAttribute("message", "Giáo viên đã có đủ nhóm.");
                redirectAttributes.addFlashAttribute("messageType", "error-message");
                return "redirect:/student/list-teacher";
            }

            // Thực hiện đăng ký giáo viên
            teamService.registerTeacher(currentStudent.getTeam().getId(), teacherId);

        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/list-teacher";
        }
        return "redirect:/student/list-teacher";
    }

    @GetMapping("/teacher-details/{id}")
    @ResponseBody
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

    @GetMapping("/documents")
    public String getAllDocuments(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  Model model) {
        User currentUser = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        Student currentStudent = studentService.findStudentByUserId(currentUser.getId());
        Team currentTeam = currentStudent.getTeam();
        Teacher teacher = currentTeam.getTeacher();
        Page<Document> documentPage = documentService.getDocumentsPage(page, size, teacher);

        model.addAttribute("documents", documentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", documentPage.getTotalPages());
        model.addAttribute("totalItems", documentPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "student/document-view";
    }
}