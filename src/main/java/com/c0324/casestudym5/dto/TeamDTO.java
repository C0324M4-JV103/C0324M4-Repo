package com.c0324.casestudym5.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TeamDTO {

    private Long id;

    private String name;

    private Date deadline;

    private Integer memberCount;

    private Integer status;
}
