package com.c0324.casestudym5.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping(value = {"/","/home"})
    public String homePage(){
        return "home-page";
    }

}
