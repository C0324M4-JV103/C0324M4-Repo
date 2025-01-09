package com.c0324.casestudym5.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class RegisterTeamDTO {
    private String topicName;
    private String topicContent;
    private MultipartFile topicDescription;
}