package com.c0324.casestudym5.dto;

import com.c0324.casestudym5.model.Teacher;
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

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherDTO {
    private Long id;

    @Pattern(regexp = "^[\\p{L}\\s-]{5,50}$", message = "Tên chỉ bao gồm chữ cái, khoảng trắng và có độ dài từ 5 đến 50 ký tự")
    @NotEmpty(message = "Tên không được để trống")
    private String name;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "Email không hợp lệ")
    @NotEmpty(message = "Email không được để trống")
    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @NotEmpty(message = "Giới tính không được để trống")
    private String gender;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    @NotEmpty(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @Size(min = 5, max = 50, message = "Địa có độ dài từ 5 đến 50 ký tự")
    @NotEmpty(message = "Địa chỉ không được để trống")
    private String address;

    @NotNull(message = "Học vị không được để trống")
    private Teacher.Degree degree;

    @NotNull(message = "Khoa không được để trống")
    private Long facultyId;

    @NotNull(message = "Ảnh không được để trống")
    private MultipartFile avatar;

    private MultipartFile multipartFile;

    public TeacherDTO(Teacher teacher) {
        this.id = teacher.getId();
        this.name = teacher.getUser().getName();
        this.email = teacher.getUser().getEmail();
        this.dob = teacher.getUser().getDob();
        this.gender = teacher.getUser().getGender().name();
        this.phoneNumber = teacher.getUser().getPhoneNumber();
        this.address = teacher.getUser().getAddress();
        this.degree = teacher.getDegree();
    }
}
