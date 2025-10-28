// src/main/java/com/example/demo/controller/web/AuthViewController.java
package com.example.demo.controller.web;

import com.example.demo.model.request.RegisterRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AuthViewController {

    @GetMapping("/register")
    public ModelAndView registerPage() {
        return new ModelAndView("web/register").addObject("form", new RegisterRequest());
    }

    @GetMapping("/otp")
    public ModelAndView otpPage(String key) {
        return new ModelAndView("web/otp").addObject("key", key);
    }
}
