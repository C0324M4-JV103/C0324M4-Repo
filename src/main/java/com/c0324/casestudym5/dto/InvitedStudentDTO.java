package com.c0324.casestudym5.dto;

import com.c0324.casestudym5.model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InvitedStudentDTO {
    private Long id;
    private String name;
    private String email;
    private Clazz clazz;
    private String phoneNumber;
    private Date dob;
    private String address;
    private User.Gender gender;
    private MultiFile avatar;
    private String status;


}
