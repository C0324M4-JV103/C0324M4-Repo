package com.c0324.casestudym5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Casestudym5Application {

    public static void main(String[] args) {
        SpringApplication.run(Casestudym5Application.class, args);
    }

}
