package com.c0324.casestudym5.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProgressReportDTO {
    private Integer phaseNumber;
    private Integer phaseProgressPercent;
    private MultipartFile reportFile;
    private String reportContent;
    private String reportLink;

    public ProgressReportDTO(Integer phaseNumber) {
        this.phaseNumber = phaseNumber;
    }
}
