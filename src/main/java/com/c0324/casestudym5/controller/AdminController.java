package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.StudentSearchDTO;
import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.repository.ClassRepository;
import com.c0324.casestudym5.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;


@RequestMapping("/admin")
@Controller
public class AdminController {

    private final TeacherService teacherService;
    private final FacultyService facultyService;
    private final UserService userService;
    private final MultiFileService multiFileService;
    private final StudentService studentService;
    private final ClassRepository classRepository;
    private final FirebaseService firebaseService;


    @Autowired
    public AdminController(TeacherService teacherService, StudentService studentService,
                           ClassRepository classRepository , FacultyService facultyService,
                           UserService userService, MultiFileService multiFileService, FirebaseService firebaseService) {
        this.teacherService = teacherService;
        this.facultyService = facultyService;
        this.userService = userService;
        this.multiFileService = multiFileService;
        this.studentService = studentService;
        this.classRepository = classRepository;
        this.firebaseService = firebaseService;
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
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("faculties", facultyService.findAll());
            return "admin/teacher/teacher-create";
        }

        try {
            userService.createUser(teacherDTO.getUserDTO());
            Teacher newTeacher = new Teacher();
            newTeacher.setDegree(teacherDTO.getDegree());

            Optional<Faculty> facultyOptional = facultyService.findById(teacherDTO.getFacultyId());
            if (facultyOptional.isEmpty()) {
                model.addAttribute("error", "Khoa không hợp lệ.");
                model.addAttribute("faculties", facultyService.findAll());
                return "admin/teacher/teacher-create";
            }

            newTeacher.setFaculty(facultyOptional.get());
            newTeacher.setUser(userService.findByEmail(teacherDTO.getUserDTO().getEmail()));
            teacherService.save(newTeacher);

            redirectAttributes.addFlashAttribute("toastMessage", "Tạo giáo viên thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã có lỗi trong quá trình tạo giáo viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
        }

        return "redirect:/admin/teacher";
    }

    @PostMapping("/teacher/create-avatar")
    public String changeTeacherAvatar(@RequestParam("avatar") MultipartFile avatar, Model model) {
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

        return "redirect:/admin/create";
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

}
