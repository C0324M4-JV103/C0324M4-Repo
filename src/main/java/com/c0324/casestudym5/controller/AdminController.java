package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.StudentDTO;
import com.c0324.casestudym5.dto.StudentSearchDTO;
import com.c0324.casestudym5.dto.TeacherDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.repository.ClassRepository;
import com.c0324.casestudym5.service.*;
import com.c0324.casestudym5.service.impl.ClazzService;
import com.c0324.casestudym5.util.DateTimeUtil;
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
    private final FacultyService facultyService;

    @Autowired
    public AdminController(TeacherService teacherService, StudentService studentService,
                           ClassRepository classRepository, ClazzService clazzService,
                           UserService userService, FacultyService facultyService) {
        this.teacherService = teacherService;

        this.studentService = studentService;
        this.classRepository = classRepository;
        this.clazzService = clazzService;
        this.userService = userService;
        this.facultyService = facultyService;
    }

    // Teacher Functionality
    @GetMapping("/teacher")
    public String getAllTeachers(@RequestParam(required = false) String email,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String phoneNumber,  // Sửa lại đây
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size, Model model) {
        Page<Teacher> teacherPage;

        // Kiểm tra điều kiện tìm kiếm
        if ((email != null && !email.isEmpty()) ||
                (name != null && !name.isEmpty()) ||
                (phoneNumber != null && !phoneNumber.isEmpty())) {
            teacherPage = teacherService.searchTeachers(email, name, phoneNumber, PageRequest.of(page, size));
        } else {
            teacherPage = teacherService.getTeachersPage(page, size);
        }

        model.addAttribute("teachers", teacherPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", teacherPage.getTotalPages());
        model.addAttribute("totalItems", teacherPage.getTotalElements());
        model.addAttribute("email", email);
        model.addAttribute("name", name);
        model.addAttribute("phoneNumber", phoneNumber);

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
        // Trim dữ liệu đầu vào
        teacherDTO.setEmail(teacherDTO.getEmail().trim());
        teacherDTO.setName(teacherDTO.getName().trim());

        if (bindingResult.hasErrors()) {
            model.addAttribute("faculties", facultyService.findAll());
            return "admin/teacher/teacher-create";
        }

        // Kiểm tra tuổi >= 22
        if (teacherDTO.getDob() != null) {
            int age = calculateAge(teacherDTO.getDob());
            if (age < 22) {
                bindingResult.rejectValue("dob", "error.teacherDTO", "Giáo viên phải đủ 22 tuổi.");
                model.addAttribute("faculties", facultyService.findAll());
                return "admin/teacher/teacher-create";
            }
        }

        try {
            if (userService.existsByEmail(teacherDTO.getEmail())) {
                bindingResult.rejectValue("email", "error.teacherDTO", "Email đã tồn tại.");
                model.addAttribute("faculties", facultyService.findAll());
                return "admin/teacher/teacher-create";
            }
            teacherService.createNewTeacher(teacherDTO, avatar);

            redirectAttributes.addFlashAttribute("toastMessage", "Thêm giáo viên thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã có lỗi trong quá trình thêm giáo viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
            System.out.println(e.getMessage());
        }

        return "redirect:/admin/teacher";
    }

    @GetMapping("/teacher/edit/{id}")
    public String editTeacherForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Teacher> teacherOptional = teacherService.getTeacherById(id);

        if (teacherOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy giáo viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
            return "redirect:/admin/teacher";
        }

        Teacher teacher = teacherOptional.get();
        TeacherDTO teacherDTO = new TeacherDTO(teacher);

        model.addAttribute("teacherDTO", teacherDTO);
        model.addAttribute("faculties", facultyService.findAll());

        return "admin/teacher/teacher-edit";
    }

    @PostMapping("/teacher/edit/{id}")
    public String editTeacher(@PathVariable Long id,
                              @Valid @ModelAttribute("teacherDTO") TeacherDTO teacherDTO,
                              BindingResult bindingResult,
                              @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        // Trim dữ liệu đầu vào
        teacherDTO.setEmail(teacherDTO.getEmail().trim());
        teacherDTO.setName(teacherDTO.getName().trim());

        if (bindingResult.hasErrors()) {
            model.addAttribute("faculties", facultyService.findAll());
            return "admin/teacher/teacher-edit";
        }

        // Kiểm tra tuổi >= 22
        if (teacherDTO.getDob() != null) {
            int age = calculateAge(teacherDTO.getDob());
            if (age < 22) {
                bindingResult.rejectValue("dob", "error.teacherDTO", "Giáo viên phải đủ 22 tuổi.");
                model.addAttribute("faculties", facultyService.findAll());
                return "admin/teacher/teacher-edit";
            }
        }

        try {
            Optional<Teacher> existingTeacher = teacherService.getTeacherById(id);
            if (!teacherDTO.getEmail().equals(existingTeacher.get().getUser().getEmail()) && userService.existsByEmail(teacherDTO.getEmail())) {
                bindingResult.rejectValue("email", "error.teacherDTO", "Email đã tồn tại.");
                model.addAttribute("faculties", facultyService.findAll());
                return "admin/teacher/teacher-edit";
            }
            teacherService.editTeacher(id, teacherDTO, avatar);
            redirectAttributes.addFlashAttribute("toastMessage", "Cập nhật giáo viên thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã có lỗi trong quá trình cập nhật.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
            System.out.println(e.getMessage());
        }

        return "redirect:/admin/teacher";
    }

    @PostMapping("/teacher/delete/{id}")
    public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteTeacherById(id);
            redirectAttributes.addFlashAttribute("toastMessage", "Xóa giáo viên thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã xảy ra lỗi khi xóa giáo viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
        }
        return "redirect:/admin/teacher";
    }



    // CalculateAge
    private int calculateAge(Date dob) {
        if (dob == null) {
            return 0;
        }
        return DateTimeUtil.calculateAge(dob);
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


        String trimmedEmail = studentDTO.getEmail().trim();
        String trimmedName = studentDTO.getName().trim();

        String cleanedEmail = trimmedEmail.replaceAll("\\s+", " ");
        String cleanedName = trimmedName.replaceAll("\\s+", " ");

        studentDTO.setEmail(cleanedEmail);
        studentDTO.setName(cleanedName);

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
                bindingResult.rejectValue("email", "error.studentDTO", "Email đã tồn tại.");
                model.addAttribute("clazzes", clazzService.getAllClazzes());
                return "admin/student/student-create";
            }

            if (studentService.existsByCode(studentDTO.getCode())) {
                bindingResult.rejectValue("code", "error.studentDTO", "Mã sinh viên đã tồn tại.");
                model.addAttribute("clazzes", clazzService.getAllClazzes());
                return "admin/student/student-create";
            }

            if (userService.existsByPhoneNumber(studentDTO.getPhoneNumber())) {
                bindingResult.rejectValue("phoneNumber", "error.studentDTO", "Số điện thoại đã tồn tại.");
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

        if (studentOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy sinh viên.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
            return "redirect:/admin/student";
        }

        Student student = studentOptional.get();
        StudentDTO studentDTO = new StudentDTO(student);
        studentDTO.setCode(student.getCode());

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

        String trimmedEmail = studentDTO.getEmail().trim();
        String trimmedName = studentDTO.getName().trim();

        String cleanedEmail = trimmedEmail.replaceAll("\\s+", " ");
        String cleanedName = trimmedName.replaceAll("\\s+", " ");

        studentDTO.setEmail(cleanedEmail);
        studentDTO.setName(cleanedName);


        if (bindingResult.hasErrors()) {
            model.addAttribute("clazzes", clazzService.getAllClazzes());
            return "admin/student/student-edit";
        }
        if (studentDTO.getDob() != null) {
            int age = calculateAge(studentDTO.getDob());
            if (age < 18) {
                bindingResult.rejectValue("dob", "error.studentDTO", "Sinh viên phải đủ 18 tuổi.");
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

            if (!studentDTO.getCode().equals(existingStudent.get().getCode()) && studentService.existsByCode(studentDTO.getCode())) {
                bindingResult.rejectValue("code", "error.studentDTO", "Mã sinh viên đã tồn tại.");
                model.addAttribute("clazzes", clazzService.getAllClazzes());
                return "admin/student/student-edit"; }

            if (!studentDTO.getPhoneNumber().equals(existingStudent.get().getUser().getPhoneNumber()) && userService.existsByPhoneNumber(studentDTO.getPhoneNumber())) {
                bindingResult.rejectValue("phoneNumber", "error.studentDTO", "Số điện thoại đã tồn tại.");
                model.addAttribute("clazzes", clazzService.getAllClazzes());
                return "admin/student/student-edit"; }

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




