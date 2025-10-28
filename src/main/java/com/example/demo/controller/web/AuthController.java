package com.example.demo.controller.web;

import com.example.demo.entity.Customer;
import com.example.demo.model.dto.UserDTO;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.CustomerService;
import com.example.demo.service.EmailService;
import com.example.demo.service.OtpService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.view.RedirectView;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
import com.example.demo.model.request.RegisterRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    
	@Autowired 
	private EmailService emailService;
	
	@Autowired 
	private OtpService otpService;
	
    @Autowired
    private UserService userService;
    @Autowired private CustomerService customerService;
    @Autowired 
    private PasswordEncoder passwordEncoder;
    
    private static final String PENDING_REG = "pending_register";

    // B1: Nhận form đăng ký, lưu tạm và gửi OTP
    @PostMapping("/register")
    public RedirectView register(RegisterRequest req, HttpSession session) {
        // check trùng + confirm password
        if (customerService.existsByUsername(req.getUsername()) || customerService.existsByEmail(req.getEmail())) {
            return new RedirectView("/register?error=exists");
        }
        if (req.getPassword() == null || !req.getPassword().equals(req.getConfirmPassword())) {
            return new RedirectView("/register?error=confirm");
        }

        // lưu tạm form vào session
        session.setAttribute(PENDING_REG, req);

        // tạo + gửi OTP theo email
        String otp = otpService.generateOtp(req.getEmail());
        emailService.sendOtp(req.getEmail(), otp);

        // sang trang nhập OTP, kèm key = email
        return new RedirectView("/otp?key=" + req.getEmail());
    }

    // B2: Nhập OTP → nếu đúng thì lưu Customer vào DB
    @PostMapping("/verify-otp")
    public RedirectView verifyOtp(@RequestParam String key,
                                  @RequestParam String otp,
                                  HttpSession session) {
        if (!otpService.validateOtp(key, otp)) {
            return new RedirectView("/otp?key=" + key + "&error=invalid");
        }

        RegisterRequest req = (RegisterRequest) session.getAttribute(PENDING_REG);
        if (req == null) {
            return new RedirectView("/register?error=session");
        }

        Customer c = new Customer();
        c.setFullname(req.getFullName());
        c.setPhone(req.getPhone());
        c.setEmail(req.getEmail());
        c.setUsername(req.getUsername());
        c.setPassword(passwordEncoder.encode(req.getPassword()));

        customerService.saveDirect(c);

        session.removeAttribute(PENDING_REG);
        return new RedirectView("/register?success");
    }

    

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // delegate to Spring Security normally; here return placeholder
        return "IMPLEMENT_LOGIN_WITH_SPRING_SECURITY";
    }
}