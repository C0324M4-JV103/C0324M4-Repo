package com.c0324.casestudym5.model.dto;

import com.c0324.casestudym5.model.Clazz;
import lombok.Data;

@Data
public class StudentSearchDTO {
    private String name = null;
    private String email = null;
    private Long clazzId = null;
}
