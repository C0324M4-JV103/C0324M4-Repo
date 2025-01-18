package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.StudentDTO;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.repository.ClassRepository;
import com.c0324.casestudym5.repository.MultiFileRepository;
import com.c0324.casestudym5.service.FirebaseService;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TeacherService;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.service.impl.ClazzService;
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

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;



@RequestMapping("/admin")
@Controller
public class AdminController {

    private final TeacherService teacherService;
    private final StudentService studentService;
    private final ClassRepository classRepository;
    private final ClazzService clazzService;
    private final UserService userService;
    private final FirebaseService firebaseService;
    private final MultiFileRepository multiFileRepository;

    @Autowired
    public AdminController(TeacherService teacherService, StudentService studentService, ClassRepository classRepository, ClazzService clazzService, UserService userService, FirebaseService firebaseService, MultiFileRepository multiFileRepository) {
        this.teacherService = teacherService;
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
        return "admin/teacher/teacher-list";
    }



    // CalculateAge
    private int calculateAge(Date dob) {
        if (dob == null) {
            return 0;
        }
        Calendar dobCal = Calendar.getInstance();
        dobCal.setTime(dob);
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
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
        Pageable pageable = PageRequest.of(page, 5);
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
    public String creatStudent(@Valid @ModelAttribute("studentDTO") StudentDTO studentDTO,
                                BindingResult bindingResult,
                                @RequestParam("avatar") MultipartFile avatar,
                                Model model,
                                RedirectAttributes redirectAttributes) {
       
        studentDTO.setEmail(studentDTO.getEmail().trim());
        studentDTO.setName(studentDTO.getName().trim());

        if (bindingResult.hasErrors()) {
            model.addAttribute("clazzes", clazzService.getAllClazzes());
            return "admin/student/student-create";
        }

        if (studentDTO.getDob() != null) {
            int age = calculateAge(studentDTO.getDob());
            if (age < 18) {
                bindingResult.rejectValue("dob", "error.studentDTO", "Sinh viên phải đủ 18 tuổi");
                model.addAttribute("clazzes", clazzService.getAllClazzes());
                return "admin/student/student-create";
            }
        }

        try {
            if (userService.existsByEmail(studentDTO.getEmail())) {
                bindingResult.rejectValue("email", "error.teacherDTO", "Email đã tồn tại.");
                model.addAttribute("clazzes", clazzService.getAllClazzes());
                return "admin/student/student-create";
            }
            studentService.createNewStudent(studentDTO, avatar);

            redirectAttributes.addFlashAttribute("toastMessage", "Thêm sinh viên thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã có lỗi trong quá trình thêm sinh viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
            System.out.println(e.getMessage());
        }

        return "redirect:/admin/student";
    }



    // Student Edit
    @GetMapping("/edit-student/{id}")
    public String editStudentForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Student> studentOptional = Optional.ofNullable(studentService.getStudent(id));

        if (!studentOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy sinh viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
            return "redirect:/admin/student";
        }

        Student student = studentOptional.get();
        StudentDTO studentDTO = new StudentDTO(student);

        model.addAttribute("studentDTO", studentDTO);
        model.addAttribute("clazzes", clazzService.getAllClazzes());

        return "admin/student/student-edit";
    }

    @PostMapping("/edit-student/{id}")
    public String editStudent(@PathVariable Long id,
                              @Valid @ModelAttribute("studentDTO") StudentDTO studentDTO,
                              BindingResult bindingResult,
                              @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        // Trim dữ liệu đầu vào
        studentDTO.setEmail(studentDTO.getEmail().trim());
        studentDTO.setName(studentDTO.getName().trim());

        if (bindingResult.hasErrors()) {
            model.addAttribute("clazzes", clazzService.getAllClazzes());
            return "admin/student/student-edit";
        }
        if (studentDTO.getDob() != null) {
            int age = calculateAge(studentDTO.getDob());
            if (age < 18) {
                bindingResult.rejectValue("dob", "error.teacherDTO", "Sinh viên phải đủ 18 tuổi.");
                model.addAttribute("clazzes", clazzService.getAllClazzes());
                return "admin/student/student-edit";
            }
        }

        try {
            Optional<Student> existingStudent = Optional.ofNullable(studentService.getStudent(id));
            if (!studentDTO.getEmail().equals(existingStudent.get().getUser().getEmail()) && userService.existsByEmail(studentDTO.getEmail())) {
                bindingResult.rejectValue("email", "error.teacherDTO", "Email đã tồn tại.");
                model.addAttribute("clazzes", clazzService.getAllClazzes());
                return "admin/student/student-edit";
            }
            studentService.editStudent(id, studentDTO, avatar);
            redirectAttributes.addFlashAttribute("toastMessage", "Cập nhật sinh viên thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã có lỗi trong quá trình cập nhật.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
            System.out.println(e.getMessage());
        }

        return "redirect:/admin/student";
    }

    // Student delete
    @PostMapping("/delete-student/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudentById(id);
            redirectAttributes.addFlashAttribute("toastMessage", "Xóa sinh viên thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã xảy ra lỗi khi xóa sinh viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
        }
        return "redirect:/admin/student";
    }
}





