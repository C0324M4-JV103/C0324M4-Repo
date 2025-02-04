package com.c0324.casestudym5.dto;

import com.c0324.casestudym5.model.Teacher;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocumentDTO {

    private Long id;

    @NotBlank(message = "Tên tài liệu không được để trống")
    @Size(max = 100, message = "Tên tài liệu không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    private boolean status;

    private Teacher teacher;

    @NotNull(message = "File không được để trống")
    private MultipartFile fileUrl;
}
