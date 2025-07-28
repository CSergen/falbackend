package com.reisfal.falbackend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Merhaba Reis, Branch üzerinden güncellendi!";
    }
}
