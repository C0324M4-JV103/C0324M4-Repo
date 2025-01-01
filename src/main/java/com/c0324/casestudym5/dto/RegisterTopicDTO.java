package com.c0324.casestudym5.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegisterTopicDTO {
    private String name;
    private String content;
    private MultipartFile description;
    private MultipartFile image;
}