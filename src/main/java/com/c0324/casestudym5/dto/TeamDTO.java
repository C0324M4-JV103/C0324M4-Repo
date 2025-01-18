package com.c0324.casestudym5.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TeamDTO {
    private Long id;

    @NotBlank(message = "Tên nhóm không được để trống.")
    @Size(max = 100, message = "Tên nhóm không được dài hơn 100 ký tự.")
    @Size(min = 5, message = "Tên nhóm phải chứa ít nhất 5 ký tự.")
    @Pattern(regexp = "^[\\p{L}0-9 ]+$", message = "Tên nhóm không được chứa ký tự đặc biệt.")
    private String name;

    private Date deadline;

    private Integer memberCount;

    private Integer status;

    private Long topicId;
}
