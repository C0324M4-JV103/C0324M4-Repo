package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.ChangePasswordDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.util.CommonMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePassword", new ChangePasswordDTO());
        return "/common/change-password-form";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePassword") ChangePasswordDTO changePasswordDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("changePassword", changePasswordDTO);
            return "/common/change-password-form";
        }

        if (!changePasswordDTO.isMatched()) {
            model.addAttribute("changePassword", changePasswordDTO);
            model.addAttribute("confirmError", "Xác nhận mật khẩu không khớp");
            return "/common/change-password-form";
        }

        if(changePasswordDTO.getOldPassword().equals(changePasswordDTO.getNewPassword())){
            model.addAttribute("changePassword", changePasswordDTO);
            model.addAttribute("error", "Mật khẩu mới không được trùng với mật khẩu cũ");
            return "/common/change-password-form";
        }

        try {
            userService.changePassword(changePasswordDTO);
        } catch (Exception e) {
            model.addAttribute("changePassword", changePasswordDTO);
            model.addAttribute("error", "Mật khẩu cũ không đúng");
            return "/common/change-password-form";
        }

        return "redirect:/";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", CommonMapper.mapUserToUserDTO(currentUser));
        return "/admin/edit-profile-form";
    }

    @PostMapping("/edit-profile")
    public String editProfile(@Valid @ModelAttribute("user") UserDTO userDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userDTO);
            return "/admin/edit-profile-form";
        }
        try {
            userService.updateProfile(userDTO);
        } catch (Exception e) {
            model.addAttribute("user", userDTO);
            model.addAttribute("emailError", "Email đã tồn tại");
            return "/admin/edit-profile-form";
        }
        return "redirect:/";
    }

    @PostMapping("/change-avatar")
    public String showChangeAvatarForm(@RequestParam("avatar") MultipartFile avatar, Model model) {
        String fileName = avatar.getOriginalFilename();
        long fileSize = avatar.getSize();
        long maxFileSize = 5 * 1024 * 1024; // 5MB

        if (fileName != null && (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg"))) {
            if (fileSize <= maxFileSize) {
                userService.changeAvatar(avatar);
            } else {
                model.addAttribute("imageError", "Kích thước ảnh không được vượt quá 5MB");
            }
        } else {
            model.addAttribute("imageError", "Chỉ hỗ trợ ảnh có định dạng jpg, jpeg, png");
        }

        return "/admin/edit-profile-form";
    }

}
