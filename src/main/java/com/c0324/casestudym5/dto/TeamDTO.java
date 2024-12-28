package com.c0324.casestudym5.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {
    @NotBlank(message = "Tên nhóm không được để trống.")
    @Size(max = 100, message = "Tên nhóm không được dài hơn 100 ký tự.")
    @Size(min = 5, message = "Tên nhóm ít nhất 5 ký tự.")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "Tên nhóm không được chứa ký tự đặc biệt.")
    private String name;
}