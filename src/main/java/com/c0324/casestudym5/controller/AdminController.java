package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.StudentSearchDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.repository.ClassRepository;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TeacherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("/admin")
@Controller
public class AdminController {

    private final TeacherService teacherService;
    private final FacultyService facultyService;
    private final UserService userService;
    private final MultiFileService multiFileService;
    private final StudentService studentService;
    private final ClassRepository classRepository;
    private final ClazzService clazzService;
    private final UserService userService;
    private final FirebaseService firebaseService;
    private final MultiFileRepository multiFileRepository;

    @Autowired
    public AdminController(TeacherService teacherService, StudentService studentService, ClassRepository classRepository, ClazzService clazzService, UserService userService, FirebaseService firebaseService, MultiFileRepository multiFileRepository) {
        this.teacherService = teacherService;
        this.facultyService = facultyService;
        this.userService = userService;
        this.multiFileService = multiFileService;
        this.studentService = studentService;
        this.classRepository = classRepository;
        this.clazzService = clazzService;
        this.userService = userService;
        this.firebaseService = firebaseService;
        this.multiFileRepository = multiFileRepository;
    }

    // Teacher Functionality
    @GetMapping("/teacher")
    public String getAllTeachers(@RequestParam(required = false) String searchQuery,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size, Model model) {
        Page<Teacher> teacherPage;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            teacherPage = teacherService.searchTeachers(searchQuery, page, size);
        } else {
            // Nếu không có searchQuery, lấy tất cả giáo viên với phân trang
            teacherPage = teacherService.getTeachersPage(page, size);
        }
        model.addAttribute("teachers", teacherPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", teacherPage.getTotalPages());
        model.addAttribute("totalItems", teacherPage.getTotalElements());
        model.addAttribute("searchQuery", searchQuery);

        if (model.containsAttribute("toastMessage")) {
            String toastMessage = (String) model.getAttribute("toastMessage");
            String toastType = (String) model.getAttribute("toastType");
            model.addAttribute("toastMessage", toastMessage);
            model.addAttribute("toastType", toastType);
        }
        return "admin/teacher/teacher-list";

    }

    @GetMapping("/teacher/create")
    public String createTeacherForm(Model model) {
        model.addAttribute("user", new UserDTO());
        model.addAttribute("teacherDTO", new TeacherDTO());
        model.addAttribute("faculties", facultyService.findAll());
        model.addAttribute("users", userService.fillAll());
        return "admin/teacher/teacher-create";
    }

    @PostMapping("/teacher/create")
    public String createTeacher(@Valid @ModelAttribute("teacherDTO") TeacherDTO teacherDTO,
                                BindingResult bindingResult,
                                @RequestParam("avatar") MultipartFile avatar,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("faculties", facultyService.findAll());
            return "admin/teacher/teacher-create";
        }

        try {
            if (userService.existsByEmail(teacherDTO.getEmail())) {
                bindingResult.rejectValue("email", "error.teacherDTO", "Email đã tồn tại.");
                model.addAttribute("faculties", facultyService.findAll());
                return "admin/teacher/teacher-create";
            }
            teacherService.createNewTeacher(teacherDTO, avatar);

            redirectAttributes.addFlashAttribute("toastMessage", "Thêm sinh viên thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã có lỗi trong quá trình thêm sinh viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
            e.printStackTrace();
        }

        return "redirect:/admin/teacher";
    }




    // Student Functionality
    @GetMapping("/student")
    public String index(Model model,
                        StudentSearchDTO search,
                        @RequestParam(defaultValue = "0") int page, HttpSession session) {
        boolean isSearch = true;
        page = page > 0 ? page - 1 : page;
        if (search.getName() != null && search.getName().isEmpty()) {
            search.setName(null);
        }
        if (search.getEmail() != null && search.getEmail().isEmpty()) {
            search.setEmail(null);
        }
        if (search.getClazzId() != null && search.getClazzId().toString().isEmpty()) {
            search.setClazzId(null);
        }
        if(search.getName() == null && search.getEmail() == null && search.getClazzId() == null) {
            isSearch = false;
        }
        model.addAttribute("pageTitle", "Danh sách sinh viên");
        Pageable pageable = PageRequest.of(page, 2);
        Page<Student> students = studentService.getPageStudents(pageable, search);
        model.addAttribute("students", students);
        model.addAttribute("classes", classRepository.findAll());
        model.addAttribute("search", search);
        model.addAttribute("isSearch", isSearch);
        session.setAttribute("page", page);
        return "admin/student/student-list";
    }

    // Student Create
    @GetMapping("/create-student")
    public String createStudentForm(Model model) {
        model.addAttribute("user", new UserDTO());
        model.addAttribute("studentDTO", new StudentDTO());
        model.addAttribute("clazzes", clazzService.getAllClazzes());
        return "admin/student/student-create";
    }

    @PostMapping("/create-student")
    public String createStudent(@Valid @ModelAttribute("studentDTO") StudentDTO studentDTO,
                                BindingResult bindingResult,
                                @RequestParam("avatar") MultipartFile avatar,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clazzes", clazzService.getAllClazzes());
            return "admin/student/student-create";
        }

        try {
            if (userService.existsByEmail(studentDTO.getEmail())) {
                bindingResult.rejectValue("email", "error.studentDTO", "Email đã tồn tại.");
                model.addAttribute("clazzes", clazzService.getAllClazzes());
                return "admin/student/student-create";
            }
            studentService.createNewStudent(studentDTO, avatar); // Gọi CreateNewStudent từ studentService

            redirectAttributes.addFlashAttribute("toastMessage", "Thêm sinh viên thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã có lỗi trong quá trình thêm sinh viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
            e.printStackTrace();
        }

        return "redirect:/admin/student";
    }

}





