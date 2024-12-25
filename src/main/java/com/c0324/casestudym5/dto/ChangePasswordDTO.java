package com.c0324.casestudym5.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChangePasswordDTO {


    @Size(min = 3, max = 50, message = "Mật khẩu phải có từ 3 đến 50 ký tự")
    @NotEmpty(message = "Trường này là bắt buộc")
    private String oldPassword;

    @Size(min = 3, max = 50, message = "Mật khẩu phải có từ 3 đến 50 ký tự")
    @NotEmpty(message = "Trường này là bắt buộc")
    private String newPassword;

    @Size(min = 3, max = 50, message = "Mật khẩu phải có từ 3 đến 50 ký tự")
    @NotEmpty(message = "Trường này là bắt buộc")
    private String confirmPassword;

    public boolean isMatched() {
        return newPassword.equals(confirmPassword);
    }
}
