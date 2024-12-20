package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.model.Clazz;
import com.c0324.casestudym5.service.ClazzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clazz")
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    @GetMapping("/list")
    public ResponseEntity<?> getAllClazzes() {
        List<Clazz> clazzes = clazzService.getAllClazzes();
        return new ResponseEntity<>(clazzes, HttpStatus.OK);
    }
}
