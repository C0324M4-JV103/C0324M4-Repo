package com.c0324.casestudym5.dto;

import com.c0324.casestudym5.model.Teacher;
import jakarta.validation.Valid;
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
    private Teacher.Degree degree;

    @NotNull(message = "Khoa không được để trống")
    private Long facultyId;

    @Valid
    private UserDTO userDTO;
}
