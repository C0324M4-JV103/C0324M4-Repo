package com.c0324.casestudym5.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
        import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegisterTopicDTO {
    @NotEmpty(message = "Name is required")
    private String name;

    @NotEmpty(message = "Content is required")
    private String content;

    @NotNull(message = "Image is required")
    private MultipartFile image;

    @NotNull(message = "Description is required")
    private MultipartFile description;
}