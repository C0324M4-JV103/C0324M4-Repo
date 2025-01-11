package com.c0324.casestudym5.dto;

import com.c0324.casestudym5.model.MultiFile;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProgressReportDTO {
    private Integer phaseNumber;
    private Integer phaseProgressPercent;
    private Integer status;
    private MultiFile reportFile;
    private String reportContent;
}
