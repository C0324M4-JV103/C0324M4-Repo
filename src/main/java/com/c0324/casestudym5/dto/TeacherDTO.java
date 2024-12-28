package com.c0324.casestudym5.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherDTO {
    private Long id;

    @NotNull(message = "Học vị không được để trống")
    private String degree;

    @NotNull(message = "Khoa không được để trống")
    private Long facultyId;

    @NotNull(message = "Thông tin người dùng không được để trống")
    private UserDTO userDTO;
}
