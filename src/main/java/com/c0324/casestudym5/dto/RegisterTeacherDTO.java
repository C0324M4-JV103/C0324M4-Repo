package com.c0324.casestudym5.dto;

import com.c0324.casestudym5.model.MultiFile;
import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterTeacherDTO {
    private Long id;
    private String name;
    private String email;
    private Teacher.Degree degree;
    private String phoneNumber;
    private Date dob;
    private String address;
    private User.Gender gender;
    private MultiFile avatar;
    private String facutly;
}
