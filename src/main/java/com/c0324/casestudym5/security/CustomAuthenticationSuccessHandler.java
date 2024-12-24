package com.c0324.casestudym5.security;

import com.c0324.casestudym5.model.Role;
import com.c0324.casestudym5.model.User;

import com.c0324.casestudym5.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;

    public CustomAuthenticationSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // Get the email of the user who logged in
        String email = authentication.getName();

        User theUser = userService.findByEmail(email);

        // Set the user in the session
        HttpSession session = request.getSession();
        session.setAttribute("user", theUser);

        Set<Role> roles = theUser.getRoles();

//        if (isAdmin(roles)) {
//            response.sendRedirect(request.getContextPath() + "/admin/home");
//            return;
//        }

        response.sendRedirect(request.getContextPath());
    }

//        private boolean isAdmin(Set<Role> roles) {
//            return roles.stream().anyMatch(role -> role.getName().toString().equals("ROLE_ADMIN"));
//        }
}
