package com.c0324.casestudym5.dto;

import lombok.Data;

@Data
public class StudentSearchDTO {
    private String name = null;
    private String email = null;
    private Long clazzId = null;
}
