package com.c0324.casestudym5.dto;

import com.c0324.casestudym5.model.Student;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentDTO {
    private Long id;

    @Pattern(regexp = "^[\\p{L}\\s-]{5,50}$", message = "Tên chỉ bao gồm chữ cái, khoảng trắng và có độ dài từ 5 đến 50 ký tự")
    @NotEmpty(message = "Tên không được để trống")
    private String name;

    @Pattern(regexp = "^HV-\\d{3}$", message = "Mã sinh viên không hợp lệ. Định dạng phải là HV-XXX")
    @NotEmpty(message = "Mã sinh viên không được để trống")
    private String code;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "Email không hợp lệ")
    @NotEmpty(message = "Email không được để trống")
    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Ngày sinh không được để trống")
    private Date dob;

    @NotEmpty(message = "Giới tính không được để trống")
    private String gender;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    @NotEmpty(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @Size(min = 5, max = 50, message = "Địa có độ dài từ 5 đến 50 ký tự")
    @NotEmpty(message = "Địa chỉ không được để trống")
    private String address;

    private MultipartFile avatar;

    @NotNull(message = "Lớp học không được để trống")
    private Long clazzId;


    private MultipartFile multipartFile;

    private String avatarUrl;

    public StudentDTO(Student student) {
        this.id = student.getId();
        this.name = student.getUser().getName();
        this.email = student.getUser().getEmail();
        this.dob = student.getUser().getDob();
        this.gender = student.getUser().getGender().name();
        this.phoneNumber = student.getUser().getPhoneNumber();
        this.address = student.getUser().getAddress();
        this.clazzId = student.getClazz().getId();
        this.avatarUrl = student.getUser().getAvatar() != null ? student.getUser().getAvatar().getUrl() : null;
    }


}
